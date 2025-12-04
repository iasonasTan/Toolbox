package com.app.toolbox.tools.stopwatch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.ReceiverOwner;
import com.app.toolbox.utils.IllegalIntentContentsException;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.Objects;
import java.util.function.Consumer;

public class StopwatchFragment extends PageFragment {
    public static final String STRING_ID               = "toolbox.page.STOPWATCH_PAGE";
    static final String NOTIFICATION_CHANNEL_ID        = "toolbox.stopwatch.notificationChannel";
    public static final String ACTION_CHANGE_FRAGMENT  = "toolbox.stopwatch.changeFragment";
    public static final String FRAGMENT_NAME_EXTRA     = "toolbox.stopwatch.fragmentName";
    public static final String FRAGMENT_MAIN_ID        = "toolbox.stopwatch.fragmentMain";
    public static final String FRAGMENT_SETTINGS_ID    = "toolbox.stopwatch.fragmentSettings";

    private final SettingsFragment mSettingsFragment           = new SettingsFragment();
    private final StopwatchMainFragment mStopwatchMainFragment = new StopwatchMainFragment();

    private final BroadcastReceiver mChangeFragmentReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Utils.checkIntent(intent, FRAGMENT_NAME_EXTRA);
            Fragment fragmentToSet = switch(
                    Objects.requireNonNull(intent.getStringExtra(FRAGMENT_NAME_EXTRA))) {
                case FRAGMENT_MAIN_ID ->  mStopwatchMainFragment;
                case FRAGMENT_SETTINGS_ID -> mSettingsFragment;
                default -> throw new IntentContentsMissingException();
            };
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragmentToSet)
                    .commit();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ContextCompat.registerReceiver(requireContext(), mChangeFragmentReceiver, new IntentFilter(ACTION_CHANGE_FRAGMENT), ContextCompat.RECEIVER_NOT_EXPORTED);
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, mStopwatchMainFragment).commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stopwatch_root, container, false);
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.stopwatch_icon);
    }

    @Override
    protected String fragmentName() {
        return STRING_ID;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(mChangeFragmentReceiver);
    }

    public static class StopwatchMainFragment extends Fragment implements ReceiverOwner {
        static final String ACTION_UPDATE_VIEW  = "toolbox.stopwatch.updateStopwatch";
        static final String ELAPSED_TIME_EXTRA  = "toolbox.stopwatch.elapsedTimeExtra";
        static final String ACTION_CHANGE_STATE = "toolbox.stopwatch.changeState";
        static final String STATE_TYPE_EXTRA    = "toolbox.stopwatch.stateTypeExtra";
        static final String PREFERENCES_NAME    = "toolbox.stopwatch.preferencesName";
        static final String MILLIS_PREFERENCE   = "toolbox.stopwatch.prefsMillis";

        private Button mStartButton, mResetButton, mStopButton;
        private TextView mTimeView;
        private boolean mShowMillis;

        private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                switch (Objects.requireNonNull(intent.getAction())) {
                    case ACTION_UPDATE_VIEW:
                        long time = intent.getLongExtra(ELAPSED_TIME_EXTRA, 0);
                        String timeStr =Utils.longToTime(time, mShowMillis);
                        Log.d("stopwatch_service", "Updating time, new time is: "+timeStr);
                        mTimeView.setText(timeStr);
                        break;
                    case ACTION_CHANGE_STATE:
                        UIState type = intent.getParcelableExtra(STATE_TYPE_EXTRA, UIState.class);
                        if (type == null) throw new IntentContentsMissingException();
                        setUiState(type);
                        break;
                    default:
                        throw new IllegalIntentContentsException();
                }
            }
        };

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_stopwatch, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Stopwatch Notifications", NotificationManager.IMPORTANCE_HIGH);
            requireContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);

            mStartButton = view.findViewById(R.id.start_button);
            mResetButton = view.findViewById(R.id.reset_button);
            mTimeView = view.findViewById(R.id.time_view);
            mStopButton = view.findViewById(R.id.stop_button);

            addButtonListeners(view);

            SharedPreferences preferences = requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            mShowMillis = preferences.getBoolean(MILLIS_PREFERENCE, false);
            adjustTimeView(mShowMillis);
            mTimeView.setText(Utils.longToTime(StopwatchService.sUntilStartTime + StopwatchService.sFromStartTime, mShowMillis));
        }

        private void addButtonListeners(@NonNull View view) {
            Consumer<String> intentSender = action -> {
                Intent intent = new Intent(requireContext(), StopwatchService.class);
                intent.setAction(action);
                requireContext().startForegroundService(intent);
            };
            mStartButton.setOnClickListener(v -> {
                setUiState(UIState.RUNNING);
                intentSender.accept(StopwatchService.ACTION_START_TIMER);
            });
            mStopButton.setOnClickListener(v -> {
                setUiState(UIState.PAUSED);
                intentSender.accept(StopwatchService.ACTION_STOP_TIMER);
            });
            mResetButton.setOnClickListener(v -> {
                setUiState(UIState.BEGINNING);
                intentSender.accept(StopwatchService.ACTION_RESET_TIMER);
            });
            view.findViewById(R.id.stopwatch_settings).setOnClickListener(v -> {
                Intent intent = new Intent(ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
                intent.putExtra(FRAGMENT_NAME_EXTRA, FRAGMENT_SETTINGS_ID);
                intent.putExtra(SettingsFragment.SHOW_MILLIS, mShowMillis);
                requireContext().sendBroadcast(intent);
            });
        }

        private void adjustTimeView(boolean millis) {
            mTimeView.setMaxLines(1);
            String template = millis?"00:00:00.00 ":"00:00:00 ";
            mTimeView.setText(template);
            mTimeView.measure(0, 0);
            int width = mTimeView.getMeasuredWidth();
            mTimeView.setWidth(width);
        }

        @Override
        public void onResume() {
            super.onResume();
            IntentFilter filter = new IntentFilter(ACTION_UPDATE_VIEW);
            filter.addAction(ACTION_CHANGE_STATE);
            ContextCompat.registerReceiver(requireContext(), mBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            MainActivity.sReceiverOwners.add(this);

            UIState uiState = UIState.RUNNING;
            if (!StopwatchService.sIsRunning) {
                uiState = StopwatchService.sStartTime<0?UIState.BEGINNING:UIState.PAUSED;
            }
            setUiState(uiState);
        }

        public void setUiState(@NonNull UIState state) {
            Log.d("debug-info", "Stopwatch ui state updated to "+state.string);
            switch(state) {
                case BEGINNING:
                    mResetButton.setVisibility(View.GONE);
                    mStartButton.setVisibility(View.VISIBLE);
                    mStopButton.setVisibility(View.GONE);
                    break;
                case RUNNING:
                    mResetButton.setVisibility(View.GONE);
                    mStartButton.setVisibility(View.GONE);
                    mStopButton.setVisibility(View.VISIBLE);
                    break;
                case PAUSED:
                    mResetButton.setVisibility(View.VISIBLE);
                    mStartButton.setVisibility(View.VISIBLE);
                    mStopButton.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void unregisterReceivers(Context context) {
            context.unregisterReceiver(mBroadcastReceiver);
        }
    }

    public static class SettingsFragment extends Fragment {
        static final String SHOW_MILLIS = "toolbox.stopwatch.showMillis";
        private CheckBox mShowMillisCheckBox;

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mShowMillisCheckBox = view.findViewById(R.id.show_millis);
            view.findViewById(R.id.save_settings).setOnClickListener(ignored -> saveAndExit());
            SharedPreferences preferences = requireContext().getSharedPreferences(StopwatchMainFragment.PREFERENCES_NAME, Context.MODE_PRIVATE);
            mShowMillisCheckBox.setChecked(preferences.getBoolean(StopwatchMainFragment.MILLIS_PREFERENCE, false));

            requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressed());
        }

        private final class OnBackPressed extends OnBackPressedCallback {
            public OnBackPressed() {
                super(true);
            }

            @Override
            public void handleOnBackPressed() {
                saveAndExit();
            }
        }

        private void saveAndExit() {
            boolean showMillis = mShowMillisCheckBox.isChecked();
            requireContext().getSharedPreferences(StopwatchMainFragment.PREFERENCES_NAME, Context.MODE_PRIVATE)
                    .edit().putBoolean(StopwatchMainFragment.MILLIS_PREFERENCE, showMillis).apply();
            Toast.makeText(requireContext(), R.string.settings_udpated, Toast.LENGTH_SHORT).show();

            Intent exitIntent = new Intent(ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            exitIntent.putExtra(FRAGMENT_NAME_EXTRA, FRAGMENT_MAIN_ID);
            requireContext().sendBroadcast(exitIntent);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_settings, container, false);
        }
    }
}

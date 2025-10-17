package com.app.toolbox.fragment.stopwatch;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.utils.IllegalIntentContentsException;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.Utils;

import java.util.Objects;

public class StopwatchFragment extends Fragment {
    static final String ACTION_UPDATE_STOPWATCH = "toolbox.stopwatch.updateStopwatch";
    static final String ELAPSED_TIME_EXTRA      = "toolbox.stopwatch.elapsedTimeExtra";
    static final String ACTION_CHANGE_STATE     = "toolbox.stopwatch.changeState";
    static final String STATE_TYPE_EXTRA        = "toolbox.stopwatch.stateTypeExtra";
    static final String PREFERENCES_NAME        = "toolbox.stopwatch.preferencesName";
    static final String MILLIS_PREFERENCE       = "toolbox.stopwatch.prefsMillis";

    private Button mStartButton, mResetButton, mStopButton;
    private TextView mTimeView;
    private boolean mShowMillis;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = Objects.requireNonNull(intent.getAction());
            switch (action) {
                case ACTION_UPDATE_STOPWATCH:
                    final int CONTENTS_MISSING = -16;
                    long time = intent.getLongExtra(ELAPSED_TIME_EXTRA, CONTENTS_MISSING);
                    if (time == CONTENTS_MISSING) throw new IntentContentsMissingException();
                    String timeStr =Utils.longToTime(time, mShowMillis);
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
        NotificationChannel channel = new NotificationChannel(StopwatchRootFragment.NOTIFICATION_CHANNEL_ID, "Stopwatch Notifications", NotificationManager.IMPORTANCE_LOW);
        requireContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);

        mStartButton = view.findViewById(R.id.start_button);
        mResetButton = view.findViewById(R.id.reset_button);
        mTimeView = view.findViewById(R.id.time_view);
        mStopButton = view.findViewById(R.id.stop_button);

        final Intent serviceIntent=new Intent(getActivity(), StopwatchService.class);
        mStartButton.setOnClickListener(v -> {
            setUiState(UIState.RUNNING);
            serviceIntent.setAction(StopwatchService.ACTION_START_TIMER);
            requireContext().startForegroundService(serviceIntent);
        });
        mStopButton.setOnClickListener(v -> {
            setUiState(UIState.PAUSED);
            serviceIntent.setAction(StopwatchService.ACTION_STOP_TIMER);
            requireContext().startForegroundService(serviceIntent);
        });
        mResetButton.setOnClickListener(v -> {
            setUiState(UIState.BEGINNING);
            serviceIntent.setAction(StopwatchService.ACTION_RESET_TIMER);
            requireContext().startForegroundService(serviceIntent);
        });

        SharedPreferences preferences = requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        mShowMillis = preferences.getBoolean(MILLIS_PREFERENCE, false);
        mTimeView.setMaxLines(1);
        adjustTimeViewSize();

        view.findViewById(R.id.stopwatch_settings).setOnClickListener(v -> {
            Intent intent = new Intent(StopwatchRootFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent.putExtra(StopwatchRootFragment.FRAGMENT_NAME_EXTRA, StopwatchRootFragment.FRAGMENT_SETTINGS_ID);
            intent.putExtra(SettingsFragment.SHOW_MILLIS, mShowMillis);
            requireContext().sendBroadcast(intent);
        });
    }

    private void adjustTimeViewSize() {
        String template = mShowMillis?"00:00:00.00 ":"00:00:00 ";
        mTimeView.setText(template);
        mTimeView.measure(0, 0);
        int width = mTimeView.getMeasuredWidth();
        mTimeView.setWidth(width);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_STOPWATCH);
        filter.addAction(ACTION_CHANGE_STATE);
        ContextCompat.registerReceiver(requireContext(), mBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        UIState uiState = UIState.RUNNING;
        if (!StopwatchService.sIsRunning) {
            uiState = StopwatchService.sStartTime<0?UIState.BEGINNING:UIState.PAUSED;
        }
        setUiState(uiState);
    }

    @Override
    public void onPause() {
        super.onPause();
        //requireContext().unregisterReceiver(mBroadcastReceiver);
    }

    public void setUiState(UIState state) {
        Log.d("debug-info", "Stopwatch ui state updated to "+state.string);
        switch(state.string) {
            case UIStateConstants.RUNNING_STR:
                mResetButton.setVisibility(View.GONE);
                mStartButton.setVisibility(View.GONE);
                mStopButton.setVisibility(View.VISIBLE);
                break;
            case UIStateConstants.PAUSED_STR:
                mResetButton.setVisibility(View.VISIBLE);
                mStartButton.setVisibility(View.VISIBLE);
                mStopButton.setVisibility(View.GONE);
                break;
            case UIStateConstants.BEGINNING_STR:
                mResetButton.setVisibility(View.GONE);
                mStartButton.setVisibility(View.VISIBLE);
                mStopButton.setVisibility(View.GONE);
                break;
            default:
                throw new IllegalArgumentException("Unknown state "+state);
        }
    }

}

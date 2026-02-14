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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.ReceiverOwner;
import com.app.toolbox.utils.IllegalIntentContentsException;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.Utils;

import java.util.Objects;
import java.util.function.Consumer;

/*
TODO: Clean-up code.
TODO: Remake class if needed
 */
public class StopwatchHome extends Fragment implements ReceiverOwner {
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
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case ACTION_UPDATE_VIEW:
                    long time = intent.getLongExtra(ELAPSED_TIME_EXTRA, 0);
                    String timeStr = Utils.longToTime(time, mShowMillis);
                    Log.d("stopwatch_service", "Updating time, new time is: " + timeStr);
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
        NotificationChannel channel = new NotificationChannel(StopwatchRoot.NOTIFICATION_CHANNEL_ID, "Stopwatch Notifications", NotificationManager.IMPORTANCE_HIGH);
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
            Intent intent = new Intent(StopwatchRoot.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent.putExtra(StopwatchRoot.FRAGMENT_NAME_EXTRA, StopwatchRoot.FRAGMENT_SETTINGS_ID);
            intent.putExtra(StopwatchSettings.SHOW_MILLIS, mShowMillis);
            requireContext().sendBroadcast(intent);
        });
    }

    private void adjustTimeView(boolean millis) {
        mTimeView.setMaxLines(1);
        String template = millis ? "88:88:88.88 " : "88:88:88 ";
        mTimeView.setText(template);
        mTimeView.measure(0, 0);
        int width = mTimeView.getMeasuredWidth();
        mTimeView.setWidth(width);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = Utils.intentFilter(ACTION_UPDATE_VIEW, ACTION_CHANGE_STATE);
        ContextCompat.registerReceiver(requireContext(), mBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        MainActivity.sReceiverOwners.add(this);

        UIState uiState = UIState.RUNNING;
        if (!StopwatchService.sIsRunning) {
            uiState = StopwatchService.sStartTime < 0 ? UIState.BEGINNING : UIState.PAUSED;
        }
        setUiState(uiState);
    }

    public void setUiState(@NonNull UIState state) {
        Log.d("debug-info", "Stopwatch ui state updated to " + state.string);
        switch (state) {
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

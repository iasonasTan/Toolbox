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

import com.app.toolbox.R;
import com.app.toolbox.utils.ParentPageFragment;
import com.app.toolbox.utils.Utils;

public class StopwatchHome extends Fragment {
    static final String PREFERENCES_NAME    = "toolbox.stopwatch.preferencesName";
    static final String MILLIS_PREFERENCE   = "toolbox.stopwatch.prefsMillis";

    private Button mStartButton, mResetButton, mStopButton;
    private TextView mTimeView;
    private boolean mShowMillis;

    private final BroadcastReceiver mTimeStatusReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            long time = intent.getLongExtra(StopwatchService.COUNTED_TIME_EXTRA, StopwatchService.UNEXISTING_TIME);
            String timeFormatted = Utils.longToTime(time, true);
            mTimeView.setText(timeFormatted);

            StopwatchState stopwatchState = intent.getParcelableExtra(StopwatchService.TIMER_STATE_EXTRA, StopwatchState.class);
            if(stopwatchState!=null)
                setUiState(stopwatchState);
            else
                throw new RuntimeException("WTF?!");
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stopwatch, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = Utils.intentFilter(StopwatchService.ACTION_SEND_UPDATE);
        ContextCompat.registerReceiver(requireContext(), mTimeStatusReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        NotificationChannel channel = new NotificationChannel(
                StopwatchRoot.NOTIFICATION_CHANNEL_ID, "Stopwatch Notifications", NotificationManager.IMPORTANCE_HIGH);
        requireContext()
                .getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStartButton = view.findViewById(R.id.start_button);
        mResetButton = view.findViewById(R.id.reset_button);
        mTimeView = view.findViewById(R.id.time_view);
        mStopButton = view.findViewById(R.id.stop_button);

        addButtonListeners(view);

        SharedPreferences preferences = requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        mShowMillis = preferences.getBoolean(MILLIS_PREFERENCE, false);
        adjustTimeView(mShowMillis);

        Intent request = new Intent(StopwatchService.ACTION_SEND_DATA).setPackage(requireContext().getPackageName());
        requireContext().sendBroadcast(request);
    }

    private void addButtonListeners(@NonNull View view) {
        mResetButton.setOnClickListener(new StopwatchActionsListener(StopwatchService.ACTION_RESET_TIMER, StopwatchState.BEGINNING));
        mStartButton.setOnClickListener(new StopwatchActionsListener(StopwatchService.ACTION_START_TIMER, StopwatchState.RUNNING));
        mStopButton.setOnClickListener(new StopwatchActionsListener(StopwatchService.ACTION_STOP_TIMER, StopwatchState.PAUSED));
        view.findViewById(R.id.stopwatch_settings).setOnClickListener(v -> {
            Intent intent = new Intent(ParentPageFragment.actionChangePage(StopwatchRoot.STRING_ID)).setPackage(requireContext().getPackageName());
            intent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, StopwatchSettings.class.getName());
            intent.putExtra(StopwatchSettings.SHOW_MILLIS, mShowMillis);
            requireContext().sendBroadcast(intent);
        });
    }

    private final class StopwatchActionsListener implements View.OnClickListener {
        private final String mAction;
        private final StopwatchState mState;

        private StopwatchActionsListener(String action, StopwatchState state) {
            this.mAction = action;
            this.mState = state;
        }

        @Override
        public void onClick(View v) {
            setUiState(mState);
            Intent stopwatchActionIntent = new Intent(requireContext(), StopwatchService.class);
            stopwatchActionIntent.setAction(mAction);
            requireContext().startForegroundService(stopwatchActionIntent);
        }
    }

    private void adjustTimeView(boolean millis) {
        mTimeView.setMaxLines(1);
        String template = millis ? "00:00:00.00 " : "00:00:00 ";
        mTimeView.setText(template);
        mTimeView.measure(0, 0);
        int width = mTimeView.getMeasuredWidth();
        mTimeView.setWidth(width);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(mTimeStatusReceiver);
    }

    public void setUiState(@NonNull StopwatchState state) {
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
}

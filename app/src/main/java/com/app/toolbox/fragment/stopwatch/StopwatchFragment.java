package com.app.toolbox.fragment.stopwatch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.app.toolbox.R;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.Objects;

public class StopwatchFragment extends ToolFragment {
    private Button start_button, reset_button, stop_button;
    private TextView timeView;

    @Override
    protected String fragmentName() {
        return "STOPWATCH_FRAGMENT";
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.stopwatch_icon);
    }

    // listening on event STOPWATCH_UPDATE and names of each constant of enum fragment.stopwatch.StateType
    private final BroadcastReceiver broadcastReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = Objects.requireNonNull(intent.getAction());
            if(action.equals("STOPWATCH_UPDATE")) {
                long time=intent.getLongExtra("elapsed_time", 0);
                timeView.setText(Utils.longToTime(time, true));
            } else {
                setUiState(action);
            }
        }
    };

    private void initNotifications() {
        NotificationChannel channel = new NotificationChannel("stopwatch_channel", "Stopwatch Notifications", NotificationManager.IMPORTANCE_LOW);
        requireContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stopwatch, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("STOPWATCH_UPDATE");
        for(StateType state: StateType.values()) {
            filter.addAction(state.name());
        }
        ContextCompat.registerReceiver(requireContext(), broadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

        if(TimerService.sIsRunning) {
            setUiState(StateType.RUNNING.name());
        } else {
            setUiState(StateType.PAUSED.name());
            if(TimerService.sStartTime<0) {
                setUiState(StateType.BEGINNING.name());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(broadcastReceiver);
    }

    public void setUiState(String stateText) {
        Log.d("debug-info", "stopwatch ui state updated to "+stateText);
        switch(stateText) {
            case "RUNNING":
                reset_button.setVisibility(View.GONE);
                start_button.setVisibility(View.GONE);
                stop_button.setVisibility(View.VISIBLE);
                break;
            case "PAUSED":
                reset_button.setVisibility(View.VISIBLE);
                start_button.setVisibility(View.VISIBLE);
                stop_button.setVisibility(View.GONE);
                break;
            case "BEGINNING":
                reset_button.setVisibility(View.GONE);
                start_button.setVisibility(View.VISIBLE);
                stop_button.setVisibility(View.GONE);
                break;
            default:
                throw new IllegalArgumentException("Unknown state "+stateText);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNotifications();

        start_button = view.findViewById(R.id.start_button);
        reset_button = view.findViewById(R.id.reset_button);
        timeView = view.findViewById(R.id.time_view);
        stop_button = view.findViewById(R.id.stop_button);

        final Intent serviceIntent=new Intent(getActivity(), TimerService.class);
        start_button.setOnClickListener(v -> {
            setUiState(StateType.RUNNING.name());
            serviceIntent.setAction("START_TIMER");
            requireContext().startForegroundService(serviceIntent);
        });
        stop_button.setOnClickListener(v -> {
            setUiState(StateType.PAUSED.name());
            serviceIntent.setAction("STOP_TIMER");
            requireContext().startForegroundService(serviceIntent);
        });
        reset_button.setOnClickListener(v -> {
            setUiState(StateType.PAUSED.name());
            serviceIntent.setAction("RESET_TIMER");
            requireContext().startForegroundService(serviceIntent);
        });
    }

}

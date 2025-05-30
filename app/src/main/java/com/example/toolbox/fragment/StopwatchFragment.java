package com.example.toolbox.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.toolbox.MainActivity;
import com.example.toolbox.view.navigation.NavigationItemView;
import com.game.toolbox.R;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class StopwatchFragment extends ToolFragment {
    private Button start_button, reset_button, stop_button;
    private TextView timeView;

    public StopwatchFragment(Context context) {
        super(new NavigationItemView(context, R.drawable.stopwatch_icon));
    }
    private final BroadcastReceiver timerReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long time=intent.getLongExtra("elapsed_time", 0);
            timeView.setText(longToTime(time, true));
        }
    };

    private final BroadcastReceiver updateReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("received intent with action "+intent.getAction());
            updateUI(Objects.requireNonNull(intent.getAction()));
        }
    };

    private void initNotifications() {
        NotificationChannel channel = new NotificationChannel(
                "stopwatch_channel",
                "Stopwatch Notifications",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager notificationManager =
                requireContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static String longToTime(long inputMillis, boolean showMillis) {
        final boolean INPUT_POSITIVE=inputMillis>=0;
        inputMillis=Math.abs(inputMillis);
        long secs=0, mins=0, hours=0;
        while (inputMillis>=1000) { secs+=1;inputMillis-=1000; }
        while (secs>=60) { mins+=1;secs-=60; }
        while (mins>=60) { hours+=1;mins-=60; }
        inputMillis/=10;
        String timeFormated = String.format(Locale.ENGLISH,
                "%s%02d:%02d:%02d",
                INPUT_POSITIVE?"":"-", hours, mins, secs);
        return showMillis?timeFormated+"."+inputMillis:timeFormated;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_stopwatch, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("STOPWATCH_UPDATE");
        ContextCompat.registerReceiver(requireContext(), timerReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);

        IntentFilter filter1=new IntentFilter();
        for(ActionType state: ActionType.values()) {
            filter1.addAction(state.name());
        }
        ContextCompat.registerReceiver(requireContext(), updateReceiver, filter1,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(timerReceiver);
        requireContext().unregisterReceiver(updateReceiver);
    }

    public void updateUI(String state) {
        System.out.println("ui changed "+state);
        switch(state) {
            case "START":
                reset_button.setVisibility(View.GONE);
                start_button.setVisibility(View.GONE);
                stop_button.setVisibility(View.VISIBLE);
                break;
            case "STOP":
                reset_button.setVisibility(View.VISIBLE);
                start_button.setVisibility(View.VISIBLE);
                stop_button.setVisibility(View.GONE);
                break;
            case "RESET":
                start_button.setVisibility(View.VISIBLE);
                reset_button.setVisibility(View.GONE);
                stop_button.setVisibility(View.GONE);
                 break;
            default:
                throw new IllegalArgumentException();
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

        start_button.setOnClickListener(v -> {
            updateUI(ActionType.START.name());
            Intent serviceIntent=new Intent(getActivity(), TimerService.class);
            serviceIntent.setAction("START_TIMER");
            requireActivity().startService(serviceIntent);
        });
        stop_button.setOnClickListener(v -> {
            updateUI(ActionType.STOP.name());
            Intent serviceIntent=new Intent(getActivity(), TimerService.class);
            serviceIntent.setAction("STOP_TIMER");
            requireActivity().startService(serviceIntent);
        });
        reset_button.setOnClickListener(v -> {
            updateUI(ActionType.RESET.name());
            Intent resetIntent=new Intent(getActivity(), TimerService.class);
            resetIntent.setAction("RESET_TIMER");
            requireActivity().
                    startService(resetIntent);
        });
    }

    public static final class NotificationEventListener extends Service {
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if(intent==null)
                // android is testing, or logic error
                return START_STICKY;

            Intent serviceIntent=new Intent(getApplicationContext(), TimerService.class);
            switch(Objects.requireNonNull(intent.getAction())) {
                case "STOP":
                    serviceIntent.setAction("STOP_TIMER");
                    break;
                case "START":
                    serviceIntent.setAction("START_TIMER");
                    break;
                case "RESET":
                    serviceIntent.setAction("RESET_TIMER");
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            getApplicationContext().startService(serviceIntent);
            sendBroadcast(new Intent(intent.getAction()).setPackage(getPackageName()));

            return START_STICKY;
        }
    }

    public enum ActionType {
        STOP,
        RESET,
        START;
    }

    public static class TimerService extends Service {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private Runnable timeCounter_run, updateNotification_run;
        private Consumer<Long> updateNotification_cons;
        private long fromStartTime =0, startTime=System.currentTimeMillis(), untilStartTime;
        private NotificationCompat.Builder time_notification;
        private NotificationManager notif_man;
        private static final int notificationID=1;
        private boolean running = false;

        private PendingIntent getPendingIntent(ActionType type) {
            Intent intent=new Intent(getApplicationContext(), NotificationEventListener.class);
            intent.setAction(type.toString());
            PendingIntent pe=PendingIntent.getService(getApplicationContext(), 0,
                    intent, PendingIntent.FLAG_IMMUTABLE);
            return pe;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            NotificationManagerCompat.from(getApplicationContext());

            notif_man=getApplicationContext().getSystemService(NotificationManager.class);

            updateNotification_run = () -> {
                updateNotification_cons.accept(untilStartTime + fromStartTime);

                if(running)
                    handler.postDelayed(updateNotification_run, 750);
            };

            timeCounter_run = () -> {
                fromStartTime =(System.currentTimeMillis()-startTime);
                sendTime(untilStartTime + fromStartTime);

                if(running)
                    handler.postDelayed(timeCounter_run, 50);
            };

            updateNotification_cons = t -> {
                NotificationCompat.Action action;
                NotificationCompat.Action action2=null;
                if(running) {
                    action=new NotificationCompat.Action(R.drawable.delete_icon, "Pause", getPendingIntent(ActionType.STOP));
                } else {
                    action=new NotificationCompat.Action(R.drawable.timer_icon, "Start", getPendingIntent(ActionType.START));
                    action2=new NotificationCompat.Action(R.drawable.delete_icon, "Reset", getPendingIntent(ActionType.RESET));
                }

                time_notification= new NotificationCompat.Builder(getApplicationContext(), "stopwatch_channel")
                        .setContentTitle("Stopwatch running")
                        .setContentText("Time: "+longToTime(t, false))
                        .setOnlyAlertOnce(true)
                        .addAction(action)
                        .addAction(action2)
                        .setSmallIcon(R.drawable.stopwatch_icon);

                notif_man.notify(notificationID, time_notification.build());
            };
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            final String action=Objects.requireNonNull(
                    intent.getAction(), "must specify intent.action");

            System.out.println("Service started with action "+intent.getAction());

            if (action.equals("RESET_TIMER")) {
                notif_man.cancel(1);
                untilStartTime =0;
                sendTime(0);
            } else if (action.equals("START_TIMER")) {
                startTime=System.currentTimeMillis();
                handler.post(timeCounter_run);
                handler.post(updateNotification_run);
                running=true;
            } else if (action.equals("STOP_TIMER")) {
                untilStartTime += fromStartTime;
                handler.removeCallbacks(timeCounter_run);
                handler.removeCallbacks(updateNotification_run);
                running=false;
                updateNotification_cons.accept(untilStartTime);
            }

            return Service.START_STICKY;
        }

        private void sendTime(long time) {
            Intent intent=new Intent("STOPWATCH_UPDATE").setPackage(getPackageName());
            intent.putExtra("elapsed_time", time);

            sendBroadcast(intent);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            /* i don't care, leave me alone */
            return null;
        }
    }

}

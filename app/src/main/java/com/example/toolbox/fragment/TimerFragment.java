package com.example.toolbox.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.toolbox.MainActivity;
import com.example.toolbox.Utils;
import com.example.toolbox.view.ItemView;
import com.example.toolbox.view.navigation.NavigationItemView;
import com.game.toolbox.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class TimerFragment extends Utils.ToolFragment {
    private static final Handler handler=new Handler(Looper.getMainLooper());
    private Runnable updateUI_run, updateNotification_run;
    public final TimerSetterFragment timerSetterFragment =new TimerSetterFragment();
    public final ActiveTimersFragment timersFragment=new ActiveTimersFragment();

    @Override
    protected String getName() {
        return "TIMER_FRAGMENT";
    }

    @Override
    protected NavigationItemView getNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.timer_icon);
    }

    private void initNotificationChannel() {
        NotificationChannel channel=new NotificationChannel(
                "timer_channel",
                "Timer Notifications",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager=
                requireContext().getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Consumer<InnerFragment> fm = fragment -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.timer_container, fragment)
                    .hide(fragment)
                    .commit();

            fragment.setParent(this);
        };

        fm.accept(timerSetterFragment);
        fm.accept(timersFragment);

        setFragment(timersFragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        initNotificationChannel();

        updateUI_run = () -> {
            Iterator<Timer> iterator=CountDownService.timers.iterator();
            while(iterator.hasNext()) {
                Timer timer=iterator.next();
                if(!timer.isAlive()) {
                    iterator.remove();
                } else {
                    handler.post(timer);
                }
            }
            timersFragment.loadTimersToGUI();

            handler.postDelayed(updateUI_run, 100);
        };
        handler.post(updateUI_run);

        updateNotification_run = () -> {
            for(Timer t: CountDownService.timers) {
                handler.post(t.getNotificationUpdate_run());
            }
            handler.postDelayed(updateNotification_run, 1000);
        };
        handler.post(updateNotification_run);

        return inflater.inflate(R.layout.fragment_timer_root, container, false);
    }

    private void setFragment (Fragment fragment) {
        final FragmentManager fragmentManager= getChildFragmentManager();
        for (Fragment f: fragmentManager.getFragments()) {
            fragmentManager.beginTransaction()
                    .hide(f)
                    .commit();
        }
        fragmentManager.beginTransaction()
                .show(fragment)
                .commit();
    }

    public static class CountDownService extends Service {
        private final static List<Timer> timers=new ArrayList<>();

        @Override
        public void onDestroy() {
            super.onDestroy();
            timers.forEach(Timer::unregisterReceiver);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if(intent==null)
                // system's business or logic error, reject
                return START_STICKY;

            long end_time = System.currentTimeMillis() +
                    intent.getLongExtra("TIME_MILLIS", 0);
            String name = intent.getStringExtra("TIMER_NAME");

            ItemView view = new ItemView(getApplicationContext());
            Timer timer = new Timer(getApplicationContext(), view, end_time, name);
            timers.add(timer);

            return START_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    public static class TimerSetterFragment extends InnerFragment {
        // views
        private NumberPicker hours_picker, minutes_picker, seconds_picker;
        private EditText name_input;

        private void initNumberPickers(View view) {
            final Setter s = (id, min, max) -> {
                NumberPicker np = view.findViewById(id);
                np.setMaxValue(max);
                np.setMinValue(min);
                return np;
            };

            hours_picker = s.setLimits(R.id.hours_picker, 0, 23);
            minutes_picker = s.setLimits(R.id.minutes_picker, 0, 59);
            seconds_picker = s.setLimits(R.id.seconds_picker, 0, 59);
        }

        private void initViews(View view) {
            view.findViewById(R.id.start_timer_button).setOnClickListener(e -> {
                long totalTime_millis=0;
                totalTime_millis+=hours_picker.getValue()*60*60*1000L;
                totalTime_millis+=minutes_picker.getValue()*60*1000L;
                totalTime_millis+=seconds_picker.getValue()*1000L;

                if(totalTime_millis!=0) {
                    Intent intent=new Intent(requireActivity(), CountDownService.class);
                    intent.putExtra("TIME_MILLIS", totalTime_millis);
                    intent.putExtra("TIMER_NAME", name_input.getText().toString());
                    requireActivity().startService(intent);
                    Toast.makeText(requireContext(), ContextCompat.getString(requireContext(), R.string.timer_set), Toast.LENGTH_SHORT).show();
                }

                parent.setFragment(parent.timersFragment);

                hours_picker.setValue(0);
                minutes_picker.setValue(0);
                seconds_picker.setValue(0);
                name_input.setText("");
            });

            name_input=view.findViewById(R.id.name_input);
        }

        @FunctionalInterface
        private interface Setter {
            NumberPicker setLimits(int id, int min, int max);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initNumberPickers(view);
            initViews(view);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_timer_setter, container, false);
        }

    }

    public static class ActiveTimersFragment extends InnerFragment {
        public LinearLayout timersList;

        @Override
        public void onHiddenChanged(boolean hidden) {
            super.onHiddenChanged(hidden);

            if(!hidden) {
                loadTimersToGUI();
            }
        }

        public void loadTimersToGUI() {
            timersList.removeAllViews();
            for(Timer t: CountDownService.timers) {
                timersList.addView(t.getView());
            }
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            timersList=view.findViewById(R.id.timers_list);
            view.findViewById(R.id.add_timer).setOnClickListener(l -> {
                parent.setFragment(parent.timerSetterFragment);
            });
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_timer_list, container, false);
        }
    }

    public static class Timer implements Runnable {
        private Context context;
        private final ItemView view;
        private Ringtone ringtone;
        private long endTime;
        private boolean alive=true;
        private final String eventID;
        private NotificationCompat.Builder time_notification;
        private NotificationManager notification_man;
        private int notificationID;

        private final Runnable ring_run = () -> ringtone.play(), updateNotification_run = () -> {
            long time = endTime - System.currentTimeMillis();
            time_notification=new NotificationCompat.Builder(context, "timer_channel")
                    .setContentTitle(ContextCompat.getString(context, R.string.timer_running))
                    .setContentIntent(Utils.createPendingIntent(MainActivity.getFragment(TimerFragment.class), context))
                    .setContentText(ContextCompat.getString(context, R.string.time_left)+ Utils.longToTime(time, false))
                    .setSmallIcon(R.drawable.timer_icon)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .addAction(R.id.deleteNote_button, ContextCompat.getString(context, R.string.stop), getNotificationIntent(getIntent()));
            notification_man.notify(notificationID, time_notification.build());
        };

        private final BroadcastReceiver receiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Broadcast received, stopping...");
                alive=false;
                if(ringtone!=null&&ringtone.isPlaying())
                    ringtone.stop();

                notification_man.cancel(notificationID);
                Toast.makeText(context, ContextCompat.getString(context, R.string.timer_canceled), Toast.LENGTH_SHORT).show();

                // unregister receiver
                context.unregisterReceiver(receiver);
            }
        };

        public Timer(Context ctx, ItemView view, long endTime, String name) {
            this.view = view;
            context=ctx;
            this.endTime = endTime;
            notificationID=hashCode();
            eventID="STOP_TIMER-"+notificationID;

            view.setContent(name);
            view.setOnDeleteListener(v -> context.startService(getIntent()));

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone=RingtoneManager.getRingtone(context, uri);

            IntentFilter filter=new IntentFilter(eventID);
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

            notification_man=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        public PendingIntent getNotificationIntent(Intent intent) {
            return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }

        public Intent getIntent() {
            Intent intent=new Intent(context, BroadcastManagerService.class);
            intent.putExtra("eventID", eventID);
            intent.setAction(eventID);
            return intent;
        }

        public void unregisterReceiver() {
            context.unregisterReceiver(receiver);
        }

        public static class BroadcastManagerService extends Service {
            @Override
            public int onStartCommand(Intent intent, int flags, int startId) {
                if(intent==null)
                    // system's stuff or logic error, ignoring
                    return START_STICKY;

                // send broadcast
                System.out.println("Sending Broadcast...");
                String id=intent.getStringExtra("eventID");
                Intent broadcastIntent=new Intent(id);
                broadcastIntent.setPackage(this.getPackageName());
                broadcastIntent.putExtra("eventID", id);
                sendBroadcast(broadcastIntent);

                return START_STICKY;
            }

            @Nullable
            @Override
            public IBinder onBind(Intent intent) {
                return null;
            }
        }

        public boolean isAlive() { return alive; }
        public View getView() { return view; }
        public Runnable getNotificationUpdate_run() { return updateNotification_run; }

        @Override
        public void run() {
            long time = endTime - System.currentTimeMillis();
            view.setTitle(Utils.longToTime(time, true));
            if(time<0) {
                handler.post(ring_run);
            }
        }


    }

    private static abstract class InnerFragment extends Fragment {
        protected TimerFragment parent;

        public void setParent (TimerFragment parent) {
            this.parent=parent;
        }
    }
}

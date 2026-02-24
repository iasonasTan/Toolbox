package com.app.toolbox.tools.timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.IllegalIntentContentsException;
import com.app.toolbox.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class TimerService extends Service {
    static final String SILENT_NOTIFICATION = "toolbox.timerService.notificationChannel.silent";

    // Service actions
    public static final String ACTION_ADD_TIMER  = "toolbox.timerService.actionAddTimer";
    public static final String TIMER_NAME_EXTRA  = "toolbox.timerService.nameExtra";
    public static final String TIME_DELTA_EXTRA  = "toolbox.timerService.deltaExtra";
    public static final String ACTION_STOP_ALL   = "toolbox.timerService.actionStopAll";
    public static final String ACTION_STOP_TIMER = "toolbox.timerService.actionStopTimer";
    public static final String TIMER_ID_EXTRA    = "toolbox.timerService.timerIdExtra";

    // Interface actions
    public static final String ACTION_SEND_DATA  = "toolbox.timerService.sendData";
    public static final String TIMERS_DATA_EXTRA = "toolbox.timerService.timersData";

    private NotificationCompat.Builder mNotificationBuilder;
    private final List<Timer> mTimers = new ArrayList<>();
    private final Handler mTimerHandler = new Handler(Looper.getMainLooper());

    private final Runnable mTimerUpdater = () -> {
        int n = mTimers.size();
        Data data = new Data(n);

        for(int i=0; i<n; i++) {
            Timer t = mTimers.get(i);
            data.titles[i] = t.TITLE;
            data.deltas[i] = t.getDelta();
            data.ids[i] = t.ID;

            mTimerHandler.post(t);
        }

        Intent interfaceDataIntent = new Intent(ACTION_SEND_DATA).setPackage(getPackageName());
        interfaceDataIntent.putExtra(TIMERS_DATA_EXTRA, data);
        getApplicationContext().sendBroadcast(interfaceDataIntent);
    };

    private final Runnable mTimerUpdateLoop = new Runnable() {
        @Override
        public void run() {
            mTimerUpdater.run();
            mTimerHandler.postDelayed(this, 600);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        PendingIntent showTimersPendingIntent = Utils.createShowPagePendingIntent(TimerRoot.STRING_ID, getApplicationContext());
        Intent intent = new Intent(getApplicationContext(), TimerService.class).setAction(ACTION_STOP_ALL);
        PendingIntent stopAllTimersPendingIntent = PendingIntent.getService(
                getApplicationContext(),
                ACTION_STOP_ALL.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        mNotificationBuilder = new NotificationCompat.Builder(getApplicationContext(), TimerService.SILENT_NOTIFICATION)
                .setContentIntent(showTimersPendingIntent)
                .setSmallIcon(R.drawable.timer_icon)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setOngoing(true)
                .addAction(R.drawable.timer_icon, getString(R.string.stop_all), stopAllTimersPendingIntent);


        NotificationChannel notificationChannel = new NotificationChannel(
                SILENT_NOTIFICATION,
                "Timer status",
                NotificationManager.IMPORTANCE_MIN
        );
        notificationChannel.setDescription(getString(R.string.status_about_running_timers));
        notificationChannel.setSound(null, null);
        getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION_ADD_TIMER -> {
                mTimerHandler.removeCallbacks(mTimerUpdateLoop);
                String name = intent.getStringExtra(TIMER_NAME_EXTRA);
                if(name==null||name.equals("null")|| name.isEmpty()) {
                    name = getString(R.string.unnamed);
                }
                long timeDelta = intent.getLongExtra(TIME_DELTA_EXTRA, /*DEFAULT VALUE:*/5000);
                if(timeDelta!=0) {
                    Timer timer = new Timer(getApplicationContext(), timeDelta, name);
                    mTimers.add(timer);
                }
                if(!mTimers.isEmpty()) {
                    Notification notification = mNotificationBuilder
                            .setContentTitle(mTimers.size() + getString(R.string.timers_running))
                            .build();
                    startForeground(11, notification);
                    mTimerHandler.post(mTimerUpdateLoop);
                }
            }
            case ACTION_STOP_TIMER -> {
                mTimerHandler.removeCallbacks(mTimerUpdateLoop);
                final int id = intent.getIntExtra(TIMER_ID_EXTRA, 0);
                Iterator<Timer> iter = mTimers.iterator();
                while(iter.hasNext()) {
                    Timer timer = iter.next();
                    if(timer.ID==id) {
                        timer.destroy();
                        iter.remove();
                    }
                }
                Log.d("timer_service", "Timers n: "+mTimers.size());
                if(!mTimers.isEmpty()) {
                    Log.d("timer_service", "Resending notification...");
                    startForeground(11, mNotificationBuilder
                            .setContentTitle(mTimers.size() + getString(R.string.timers_running))
                            .build());
                } else {
                    Log.d("timer_service", "Stopping service...");

                    stopSelf();
                }
                mTimerUpdater.run();
            }
            case ACTION_STOP_ALL -> {
                mTimerHandler.removeCallbacks(mTimerUpdateLoop);
                mTimers.forEach(Timer::destroy);
                mTimers.clear();
                mTimerUpdater.run();
                stopSelf();
            }
            default -> throw new IllegalIntentContentsException();
        }
        return START_STICKY;
    }

    private static final class Timer implements Runnable {
        // Android
        private final Context context;
        private final NotificationCompat.Builder mTimeNotificationBuilder;
        private final Ringtone mRingtone;
        private final NotificationManager mNotificationMan;

        // Constants
        final long END_TIME;
        final int ID;
        final String TITLE;

        Timer(Context context, long deltaTime, String name) {
            this.context = context;
            ID = hashCode();
            TITLE = name;
            END_TIME = System.currentTimeMillis() + deltaTime;

            mNotificationMan = context.getSystemService(NotificationManager.class);
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mRingtone = RingtoneManager.getRingtone(context, uri);

            Intent intent = new Intent(context, TimerService.class)
                    .setAction(ACTION_STOP_TIMER)
                    .putExtra(TIMER_ID_EXTRA, ID);
            PendingIntent pKillIntent = PendingIntent.getForegroundService(context, ID, intent, PendingIntent.FLAG_IMMUTABLE);
            mTimeNotificationBuilder = new NotificationCompat.Builder(context, TimerRoot.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(context.getString(R.string.timer_running) + " \"" + TITLE + "\"")
                    .setContentIntent(Utils.createShowPagePendingIntent(TimerRoot.STRING_ID, context))
                    .setSmallIcon(R.drawable.timer_icon)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setSilent(true)
                    .setOngoing(true)
                    .addAction(R.drawable.delete_icon, ContextCompat.getString(context, R.string.stop), pKillIntent);
        }

        public long getDelta(){
            return END_TIME - System.currentTimeMillis();
        }

        public void destroy() {
            mRingtone.stop();
            mNotificationMan.cancel(ID);
            Toast.makeText(context, context.getString(R.string.timer_canceled), Toast.LENGTH_SHORT).show();
        }

        public Notification createNotification() {
            if (System.currentTimeMillis() - END_TIME > 0) {
                mTimeNotificationBuilder.setContentTitle(context.getString(R.string.time_is_up))
                        .setContentText(context.getString(R.string.timer)+" \""+ TITLE +"\" "+context.getString(R.string.ended));
            } else {
                long elapsed_time = END_TIME - System.currentTimeMillis();
                mTimeNotificationBuilder.setContentTitle(context.getString(R.string.timer_running)+" \""+ TITLE +"\"")
                        .setContentText(context.getString(R.string.time_left) + Utils.longToTime(elapsed_time, false));
            }
            return mTimeNotificationBuilder.build();
        }

        @Override
        public void run() {
            long delta = System.currentTimeMillis() - END_TIME;
            if (delta > 0) { // finished
                if(!mRingtone.isPlaying())
                    mRingtone.play();
            }

            mNotificationMan.notify(ID, createNotification());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

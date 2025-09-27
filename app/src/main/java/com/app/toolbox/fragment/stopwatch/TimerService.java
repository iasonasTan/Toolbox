package com.app.toolbox.fragment.stopwatch;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.utils.Utils;

import java.util.Objects;
import java.util.function.Consumer;

public class TimerService extends Service {
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mTimeCounter_run, mUpdateNotification_run;
    private Consumer<Long> mUpdateNotification_cons;
    private NotificationCompat.Builder mTime_notification;
    private final int M_NOTIFICATION_ID = 1;

    static long sFromStartTime = 0, sStartTime = -1, sUntilStartTime;
    static boolean sIsRunning = false;

    private PendingIntent getPendingIntent(ActionType type) {
        Intent intent = new Intent(getApplicationContext(), BroadcastSenderService.class);
        intent.setAction(type.toString());
        return PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManagerCompat.from(getApplicationContext());
        mUpdateNotification_run = () -> {
            mUpdateNotification_cons.accept(sUntilStartTime + sFromStartTime);
            if (sIsRunning) mHandler.postDelayed(mUpdateNotification_run, 750);
        };
        mTimeCounter_run = () -> {
            sFromStartTime = (System.currentTimeMillis() - sStartTime);
            sendTime(sUntilStartTime + sFromStartTime);
            if (sIsRunning) mHandler.postDelayed(mTimeCounter_run, 50);
        };
        mUpdateNotification_cons = time_millis -> {
            NotificationCompat.Action action, action2 = null;
            if (sIsRunning) {
                action = new NotificationCompat.Action(R.drawable.delete_icon, ContextCompat.getString(getApplicationContext(), R.string.stop), getPendingIntent(ActionType.STOP));
            } else {
                action = new NotificationCompat.Action(R.drawable.timer_icon, ContextCompat.getString(getApplicationContext(), R.string.start), getPendingIntent(ActionType.START));
                action2 = new NotificationCompat.Action(R.drawable.delete_icon, ContextCompat.getString(getApplicationContext(), R.string.reset), getPendingIntent(ActionType.RESET));
            }
            mTime_notification = new NotificationCompat.Builder(getApplicationContext(), "stopwatch_channel")
                    .setContentTitle(ContextCompat.getString(getApplicationContext(), R.string.stopwatch_running))
                    .setContentIntent(Utils.createShowPendingIntent(MainActivity.getFragment(StopwatchFragment.class), getApplicationContext()))
                    .setOnlyAlertOnce(true).addAction(action).setSilent(true).setOngoing(true).addAction(action2).setSmallIcon(R.drawable.stopwatch_icon)
                    .setContentText(ContextCompat.getString(getApplicationContext(), R.string.time) + Utils.longToTime(time_millis, false));
            startForeground(M_NOTIFICATION_ID, mTime_notification.build());
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        final String action = Objects.requireNonNull(intent.getAction(), "must specify intent.action");
        switch (action) {
            case "RESET_TIMER":
                stopForeground(true);
                sUntilStartTime = 0;
                sStartTime=-1;
                sendTime(0);
                stopForeground(true);
                stopSelf();
                break;
            case "START_TIMER":
                sStartTime = System.currentTimeMillis();
                mHandler.post(mTimeCounter_run);
                mHandler.post(mUpdateNotification_run);
                sIsRunning = true;
                mUpdateNotification_run.run();
                break;
            case "STOP_TIMER":
                sUntilStartTime += sFromStartTime;
                mHandler.removeCallbacks(mTimeCounter_run);
                mHandler.removeCallbacks(mUpdateNotification_run);
                sIsRunning = false;
                mUpdateNotification_cons.accept(sUntilStartTime);
                break;
        }
        return START_STICKY;
    }

    private void sendTime(long time) {
        Intent intent = new Intent("STOPWATCH_UPDATE").setPackage(getPackageName());
        intent.putExtra("elapsed_time", time);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

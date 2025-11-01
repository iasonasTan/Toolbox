package com.app.toolbox.fragment.stopwatch;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.Utils;

import java.util.Objects;
import java.util.function.Consumer;

public class StopwatchService extends Service {
    // used by action
    public static final String ACTION_START_TIMER  = "toolbox.stopwatchService.startTimer";
    static final String ACTION_STOP_TIMER   = "toolbox.stopwatchService.stopTimer";
    static final String ACTION_RESET_TIMER  = "toolbox.stopwatchService.resetTimer";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mTimeCounter_run, mUpdateNotification_run;
    private Consumer<Long> mUpdateNotification_cons;
    private NotificationCompat.Builder mTime_notification;
    private final int NOTIFICATION_ID = 1;

    static long sFromStartTime = 0, sStartTime = -1, sUntilStartTime;
    static boolean sIsRunning = false;

    private PendingIntent getPendingIntent(ActionType type, String action) {
        Intent intent = new Intent(getApplicationContext(), BroadcastSenderService.class);
        intent.setAction(action);
        intent.putExtra(StopwatchFragment.STATE_TYPE_EXTRA, type);
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
        Log.d("pIntentCreation", "Creating pending intent showing page "+StopwatchRootFragment.STRING_ID);
        final PendingIntent pendingIntent = Utils.createShowPagePendingIntent(StopwatchRootFragment.STRING_ID, getApplicationContext());
        mUpdateNotification_cons = time_millis -> {
            NotificationCompat.Action action, action2 = null;
            if (sIsRunning) {
                action = new NotificationCompat.Action(R.drawable.delete_icon, ContextCompat.getString(getApplicationContext(), R.string.pause), getPendingIntent(ActionType.STOP, BroadcastSenderService.ACTION_STOP));
            } else {
                action = new NotificationCompat.Action(R.drawable.timer_icon, ContextCompat.getString(getApplicationContext(), R.string.start), getPendingIntent(ActionType.START, BroadcastSenderService.ACTION_START));
                action2 = new NotificationCompat.Action(R.drawable.delete_icon, ContextCompat.getString(getApplicationContext(), R.string.reset), getPendingIntent(ActionType.RESET, BroadcastSenderService.ACTION_RESET));
            }
            mTime_notification = new NotificationCompat.Builder(getApplicationContext(), StopwatchRootFragment.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(ContextCompat.getString(getApplicationContext(), R.string.stopwatch_running))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.stopwatch_icon)
                    .addAction(action)
                    .addAction(action2)
                    .setOnlyAlertOnce(true)
                    .setSilent(true)
                    .setOngoing(true)
                    .setContentText(ContextCompat.getString(getApplicationContext(), R.string.time) + Utils.longToTime(time_millis, false));
            startForeground(NOTIFICATION_ID, mTime_notification.build());
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        final String action = Objects.requireNonNull(intent.getAction(), "must specify intent.action");
        switch (action) {
            case ACTION_RESET_TIMER:
                stopForeground(Service.STOP_FOREGROUND_REMOVE);
                sUntilStartTime = 0;
                sStartTime=-1;
                sendTime(0);
                stopSelf();
                break;
            case ACTION_START_TIMER:
                sStartTime = System.currentTimeMillis();
                mHandler.post(mTimeCounter_run);
                mHandler.post(mUpdateNotification_run);
                sIsRunning = true;
                mUpdateNotification_run.run();
                break;
            case ACTION_STOP_TIMER:
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
        Intent intent = new Intent(StopwatchFragment.ACTION_UPDATE_STOPWATCH).setPackage(getPackageName());
        intent.putExtra(StopwatchFragment.ELAPSED_TIME_EXTRA, time);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

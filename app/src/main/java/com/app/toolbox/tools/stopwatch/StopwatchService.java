package com.app.toolbox.tools.stopwatch;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.tools.stopwatch.widget.StopwatchWidget;
import com.app.toolbox.utils.Utils;

import java.util.Objects;
import java.util.function.Function;

public class StopwatchService extends Service {
    public static final String ACTION_START_TIMER  = "toolbox.stopwatchService.START_TIMER";
    public static final String ACTION_STOP_TIMER   = "toolbox.stopwatchService.STOP_TIMER";
    public static final String ACTION_RESET_TIMER  = "toolbox.stopwatchService.RESET_TIMER";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    static long sFromStartTime = 0, sStartTime = -1, sUntilStartTime;
    static boolean sIsRunning = false;
    private PendingIntent mShowPagePendingIntent;

    public void countTime() {
        if(!sIsRunning) return;
        sFromStartTime = (System.currentTimeMillis() - sStartTime);
        sendTime(sUntilStartTime + sFromStartTime);
        mHandler.postDelayed(this::countTime, 50);
    }

    public void updateNotification() {
        if(!sIsRunning) return;
        updateNotification(sUntilStartTime + sFromStartTime);
        mHandler.postDelayed(this::updateNotification, 750);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mShowPagePendingIntent = Utils.createShowPagePendingIntent(StopwatchRootFragment.STRING_ID, getApplicationContext());
    }

    public void updateNotification(Long time) {
        Function<String, PendingIntent> intent = action ->
                PendingIntent.getService(getApplicationContext(), action.hashCode(), new Intent(getApplicationContext(), StopwatchService.class).setAction(action), PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Action action, action2 = null;
        if (sIsRunning) {
            action = new NotificationCompat.Action(R.drawable.delete_icon, ContextCompat.getString(getApplicationContext(), R.string.pause), intent.apply(ACTION_STOP_TIMER));
        } else {
            action = new NotificationCompat.Action(R.drawable.timer_icon, ContextCompat.getString(getApplicationContext(), R.string.start), intent.apply(ACTION_START_TIMER));
            action2 = new NotificationCompat.Action(R.drawable.delete_icon, ContextCompat.getString(getApplicationContext(), R.string.reset), intent.apply(ACTION_RESET_TIMER));
        }
        NotificationCompat.Builder timeNotification = new NotificationCompat.Builder(getApplicationContext(), StopwatchRootFragment.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(ContextCompat.getString(getApplicationContext(), R.string.stopwatch_running))
                .setContentIntent(mShowPagePendingIntent)
                .setSmallIcon(R.drawable.stopwatch_icon)
                .addAction(action)
                .addAction(action2)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setOngoing(true)
                .setContentText(ContextCompat.getString(getApplicationContext(), R.string.time) + Utils.longToTime(time, false));
        startForeground(1, timeNotification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        final String action = Objects.requireNonNull(intent.getAction(), "must specify intent.action");
        switch (action) {
            case ACTION_RESET_TIMER -> reset();
            case ACTION_START_TIMER -> start();
            case ACTION_STOP_TIMER -> stop();
        }
        return START_STICKY;
    }

    private void stop() {
        sIsRunning = false;
        updateNotification(sUntilStartTime + sFromStartTime);
        sendTime(sUntilStartTime + sFromStartTime);
        sUntilStartTime += sFromStartTime;
        mHandler.removeCallbacks(this::countTime);
        mHandler.removeCallbacks(this::updateNotification);
    }

    private void start() {
        sStartTime = System.currentTimeMillis();
        mHandler.post(this::countTime);
        mHandler.post(this::updateNotification);
        sIsRunning = true;
        updateNotification();
    }

    private void reset() {
        stop();
        stopForeground(Service.STOP_FOREGROUND_REMOVE);
        sUntilStartTime = 0;
        sStartTime=-1;
        sendTime(0);
        stopSelf();
    }

    private void sendTime(long time) {
        // send to gui
        Intent uiIntent = new Intent(StopwatchFragment.ACTION_UPDATE_VIEW).setPackage(getPackageName());
        uiIntent.putExtra(StopwatchFragment.ELAPSED_TIME_EXTRA, time);
        sendBroadcast(uiIntent);
        // send to widget
        Intent widgetIntent = new Intent(getApplicationContext(), StopwatchWidget.class);
        widgetIntent.putExtra(StopwatchWidget.TIME_EXTRA, time);
        sendBroadcast(widgetIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

package com.app.toolbox.tools.timer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class TimerService extends Service implements Runnable {
    static final String ACTION_UPDATE_TIMERS = "toolbox.timerService.updateTimers";
    static final String ACTION_STOP_ALL_TIMERS = "toolbox.timerService.stopTimers";
    public static final String TIMER_ID_EXTRA    = "toolbox.timer.timerID";
    public static final String ACTION_STOP_TIMER = "toolbox.timer.stopTimer";
    static final String SILENT_NOTIFICATION      = "toolbox.timerService.notificationChannel.silent";
    private PendingIntent mStopAllTimersPendingIntent, mShowTimersPendingIntent;
    private static final List<Timer> sTimers =new ArrayList<>();

    private Thread mThread;
    private static boolean sRunning = true;
    private static boolean sPaused = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(getApplicationContext(), TimerService.class).setAction(ACTION_STOP_ALL_TIMERS);
        mStopAllTimersPendingIntent = PendingIntent.getService(getApplicationContext(), ACTION_STOP_ALL_TIMERS.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
        mShowTimersPendingIntent = Utils.createShowPagePendingIntent(TimerFragment.STRING_ID, getApplicationContext());

        NotificationChannel notificationChannel = new NotificationChannel(SILENT_NOTIFICATION, "Timer status", NotificationManager.IMPORTANCE_MIN);
        notificationChannel.setDescription("Status about running timers.");
        notificationChannel.setSound(null, null);
        getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null) return START_STICKY;
        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION_STOP_TIMER -> {
                Utils.checkIntent(intent, TIMER_ID_EXTRA);
                int timerID = intent.getIntExtra(TIMER_ID_EXTRA, 0);
                Iterator<Timer> timerIterator = sTimers.iterator();
                while(timerIterator.hasNext()) {
                    Timer timer = timerIterator.next();
                    if(timerID==timer.getId()) {
                        timer.terminate();
                        timerIterator.remove();
                        updateTimers();
                    }
                }
            }
            case ACTION_UPDATE_TIMERS -> updateTimers();
            case ACTION_STOP_ALL_TIMERS -> {
                Log.d("timer_service", "Stopping all timers now.");
                sTimers.forEach(t -> {
                    Intent intent2 = t.createIntent();
                    startForegroundService(intent2);
                    Log.d("timer_service", "Stop timer with ID=" + t.getId());
                });
            }
            default -> throw new IntentContentsMissingException();
        }
        return START_STICKY;
    }

    private void updateTimers() {
        restartThread();
        sendStatusNotification();
        if (sTimers.isEmpty()) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
            stopSelf();
        }
    }

    public void restartThread() {
        sRunning = false;
        if (mThread != null)
            mThread.interrupt();
        mThread = new Thread(this);
        sRunning = true;
        mThread.start();
    }

    static void addTimer(Timer timer) {
        sPaused = true;
        sTimers.add(timer);
        sPaused = false;
    }

    static List<Timer> getTimers() {
        return sTimers;
    }

    void sendStatusNotification() {
        NotificationCompat.Builder mServiceNotificationBuilder = new NotificationCompat.Builder(getApplicationContext(), TimerService.SILENT_NOTIFICATION)
                .setContentIntent(mShowTimersPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setSmallIcon(R.drawable.timer_icon)
                .setContentTitle(sTimers.size() + getString(R.string.timers_running));
        if(!sTimers.isEmpty())
            mServiceNotificationBuilder.addAction(R.drawable.delete_icon, getString(R.string.stop_all), mStopAllTimersPendingIntent);
        startForeground(11, mServiceNotificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sRunning = false;
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        while(sRunning) {
            if(sPaused) continue;
            sTimers.forEach(t -> new Thread(t).start());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore, thread is interrupted
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

package com.app.toolbox.fragment.timer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.ItemView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TimerService extends Service implements Runnable {
    static final String UPDATE_TIMERS = "toolbox.timerService.updateTimers";
    static final String STOP_ALL_TIMERS = "toolbox.timerService.stopTimers";

    static final String SILENT_NOTIFICATION = "toolbox.timerService.notificationChannel.silent";
    private NotificationCompat.Builder mServiceNotificationBuilder;
    private PendingIntent mStopAllTimersPendingIntent, mShowTimersPendingIntent;

    private static final List<Timer> sTimers =new ArrayList<>();

    private Thread mThread;
    private static boolean sRunning = true;
    private static boolean sPaused = false;
    private static boolean sStartActivity = false;
    private static int sTimerID = -5;
    private static String sTimerName = "unknown";
    public static boolean sIsActivityInForeground = true;

    @Override
    public void onCreate() {
        super.onCreate();
        // stop timers pIntent
        Intent intent = new Intent(getApplicationContext(), TimerService.class);
        intent.setAction(STOP_ALL_TIMERS);
        mStopAllTimersPendingIntent = PendingIntent.getService(getApplicationContext(), 12, intent, PendingIntent.FLAG_IMMUTABLE);
        // show timers pIntent
        mShowTimersPendingIntent = Utils.createShowPagePendingIntent("TIMER_FRAGMENT", getApplicationContext());

        NotificationChannel notificationChannel = new NotificationChannel(SILENT_NOTIFICATION, "Timer status", NotificationManager.IMPORTANCE_MIN);
        notificationChannel.setDescription("Status about running timers.");
        notificationChannel.setSound(null, null);
        getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null) return START_STICKY;
        String action = Objects.requireNonNull(intent.getAction());
        if (action.equals(UPDATE_TIMERS)) {
            sRunning = false;
            if(mThread != null)
                mThread.interrupt();
            mThread = new Thread(this);
            sRunning = true;
            mThread.start();
            sendStatusNotification();
        } else if (action.equals(STOP_ALL_TIMERS)) {
            Log.d("timer_test", "Stopping all timers now.");
            sTimers.forEach(t -> {
                Intent intent2= t.createIntent();
                sendBroadcast(intent2);
                Log.d("action_spoil", "Stop also timer with ID="+t.getTimerID());
            });
        } else {
            throw new IntentContentsMissingException();
        }
        return START_STICKY;
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
        mServiceNotificationBuilder = new NotificationCompat.Builder(getApplicationContext(), TimerService.SILENT_NOTIFICATION)
                .setContentIntent(mShowTimersPendingIntent)
                .setSmallIcon(R.drawable.timer_icon)
                .setContentTitle(sTimers.size()+getString(R.string.timers_running));
        if(!sTimers.isEmpty())
            mServiceNotificationBuilder.addAction(R.drawable.delete_icon, "Stop all", mStopAllTimersPendingIntent);
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
            if(sStartActivity&&sIsActivityInForeground) {
                Log.d("activity_log", "Timers activity is removed/deprecated.");
//                PendingIntent pendingIntent = TimerAlertActivity.createPIntent(getApplicationContext(), "Timer "+sTimerName+" ended.", sTimerID);
//                Log.d("activity-log", "trying to start activity");
//                try {
//                    sStartActivity = false;
//                    pendingIntent.send();
//                } catch (PendingIntent.CanceledException e) {
//                    throw new RuntimeException(e);
//                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // ignore, thread is interrupted
            }
        }
    }

    static final class Timer implements Runnable /* ,Parcelable */ {
        private final Context context;
        private final ItemView mView;
        NotificationCompat.Builder time_notification;

        // logic
        private long mLastNotificationUpdateTimeMillis = System.currentTimeMillis();
        private boolean mIsRunning = true, mFinished = false;

        // constants
        private final long END_TIME;
        private final int TIMER_ID;

        // statics
        static Ringtone sRingtone = null;
        private static NotificationManager sNotificationMan;
        private static final Handler sHandler =new Handler(Looper.getMainLooper());

        Timer(Context ctx, ItemView view, long endTime, String name) {
            this.mView = view;
            this.END_TIME = endTime;
            context = ctx;
            TIMER_ID = hashCode()+65033; // a!=b => a+x!=b+x

            view.setContent(name.isBlank()?context.getString(R.string.unnamed):name);
            view.setOnDeleteListener(v -> {
                Intent intent= createIntent();
                context.sendBroadcast(intent);
                Log.d("action_spoil", "Stop intent sent with ID="+ getTimerID());
            });

            initNotification();
            if(sRingtone == null) {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                sRingtone = RingtoneManager.getRingtone(context, uri);
            }

            sHandler.post(this);
        }

        private Intent createIntent() {
            Intent intent = new Intent(context, StopTimerReceiver.class);
            intent.setAction("STOP_TIMER");
            intent.putExtra("timer_id", getTimerID());
            return intent;
        }

        private PendingIntent getPendingIntent(Intent intent) {
            return PendingIntent.getBroadcast(context, getTimerID(), intent, PendingIntent.FLAG_IMMUTABLE);
        }

        void terminate() {
            sRingtone.stop();
            sNotificationMan.cancel(getNotificationId());
            mIsRunning = false;
            sHandler.removeCallbacks(this);
            removeFromParent(mView);
            Toast.makeText(context, ContextCompat.getString(context, R.string.timer_canceled), Toast.LENGTH_SHORT).show();
        }

        private void removeFromParent(View view) {
            LinearLayout parent = (LinearLayout) view.getParent();
            if (parent != null) parent.removeView(view);
        }

        int getTimerID() { return TIMER_ID; }

        ItemView getView() { return mView; }
        String getName() { return mView.getContent(); }

        private void initNotification() {
            if(sNotificationMan == null) sNotificationMan = context.getSystemService(NotificationManager.class);
            time_notification = new NotificationCompat.Builder(context, TimerFragment.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(ContextCompat.getString(context, R.string.timer_running)+"("+getName()+")")
                    .setContentIntent(Utils.createShowPagePendingIntent("TIMER_FRAGMENT", context))
                    .setSmallIcon(R.drawable.timer_icon)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setSilent(true)
                    .setOngoing(true)
                    .addAction(R.drawable.delete_icon, ContextCompat.getString(context, R.string.stop), getPendingIntent(createIntent()));
        }

        public Notification createNotification() {
            if(mFinished) {
                time_notification.setContentTitle(ContextCompat.getString(context, R.string.time_is_up))
                        .setContentText(ContextCompat.getString(context, R.string.timer) + getName() +" "+ ContextCompat.getString(context, R.string.ended));
                        //.setFullScreenIntent(getPendingIntent(createIntent()), true)
            } else {
                long elapsed_time = END_TIME - System.currentTimeMillis();
                time_notification.setContentTitle(ContextCompat.getString(context, R.string.timer_running)+"("+ getName() +")")
                        .setContentText(ContextCompat.getString(context, R.string.time_left) + Utils.longToTime(elapsed_time, false));
            }
            return time_notification.build();
        }

        @Override
        public void run() {
            final long CURR_T = System.currentTimeMillis();
            final long ELL_T = END_TIME - CURR_T;
            long SEND_NOTIFICATION_EVERY_MILLIS = 800;
            if (CURR_T - mLastNotificationUpdateTimeMillis >= SEND_NOTIFICATION_EVERY_MILLIS) {
                sNotificationMan.notify(getNotificationId(), createNotification());
                mLastNotificationUpdateTimeMillis = CURR_T;
            }
            if (ELL_T < 0 && mIsRunning) {
                endTimer();
            }
            sHandler.post(() -> mView.setTitle(Utils.longToTime(ELL_T, false)));
//            sHandler.postDelayed(this, 90);
        }

        private void endTimer() {
            sStartActivity = true;
            mIsRunning = false;
            sRingtone.play();
            Log.d("action_spoil", "Stopping timer...");
            mFinished = true;
            sTimerName = getName();
            sTimerID = getTimerID();
            Intent intent = new Intent(context, TimerService.class);
            intent.setAction(TimerService.UPDATE_TIMERS);
            context.startForegroundService(intent);
        }

        public int getNotificationId() {
            return TIMER_ID;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

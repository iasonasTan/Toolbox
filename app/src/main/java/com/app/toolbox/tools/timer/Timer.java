package com.app.toolbox.tools.timer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.RemovableView;

final class Timer implements Runnable /* ,Parcelable */ {
    private final Context context;
    private final RemovableView mView;
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
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    Timer(Context ctx, RemovableView view, long endTime, String name) {
        this.mView = view;
        this.END_TIME = endTime;
        context = ctx;
        TIMER_ID = hashCode() + 65033; // a!=b => a+x!=b+x

        view.setContent(name.isBlank() ? context.getString(R.string.unnamed) : name);
        view.setOnDeleteListener(v -> {
            context.startForegroundService(createIntent());
            Log.d("stopping_timer", "Stop intent sent with ID=" + getId());
        });

        initNotification();
        if (sRingtone == null) {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            sRingtone = RingtoneManager.getRingtone(context, uri);
        }

        sHandler.post(this);
    }

    public Intent createIntent() {
        Intent intent = new Intent(context, TimerService.class);
        intent.setAction(TimerService.ACTION_STOP_TIMER);
        intent.putExtra(TimerService.TIMER_ID_EXTRA, getId());
        return intent;
    }

    private PendingIntent createPendingIntent(Intent intent) {
        return PendingIntent.getForegroundService(context, getId(), intent, PendingIntent.FLAG_IMMUTABLE);
    }

    void terminate() {
        mIsRunning = false;
        sRingtone.stop();
        sNotificationMan.cancel(getNotificationId());
        sHandler.removeCallbacks(this);
        removeFromParent(mView);
        Toast.makeText(context, ContextCompat.getString(context, R.string.timer_canceled), Toast.LENGTH_SHORT).show();
    }

    private void removeFromParent(View view) {
        LinearLayout parent = (LinearLayout) view.getParent();
        if (parent != null) parent.removeView(view);
    }

    int getId() {
        return TIMER_ID;
    }

    RemovableView getView() {
        return mView;
    }

    String getName() {
        return mView.getContent();
    }

    private void initNotification() {
        if (sNotificationMan == null)
            sNotificationMan = context.getSystemService(NotificationManager.class);
        Log.d("pIntentCreation", "Class timerService calls createShowPageIntent() (initializing)");
        time_notification = new NotificationCompat.Builder(context, TimerFragment.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(ContextCompat.getString(context, R.string.timer_running) + "(" + getName() + ")")
                .setContentIntent(Utils.createShowPagePendingIntent(TimerFragment.STRING_ID, context))
                .setSmallIcon(R.drawable.timer_icon)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setOngoing(true)
                .addAction(R.drawable.delete_icon, ContextCompat.getString(context, R.string.stop), createPendingIntent(createIntent()));
    }

    public Notification createNotification() {
        if (mFinished) {
            time_notification.setContentTitle(ContextCompat.getString(context, R.string.time_is_up))
                    .setContentText(ContextCompat.getString(context, R.string.timer) + getName() + " " + ContextCompat.getString(context, R.string.ended));
            //.setFullScreenIntent(createPendingIntent(createIntent()), true)
        } else {
            long elapsed_time = END_TIME - System.currentTimeMillis();
            time_notification.setContentTitle(ContextCompat.getString(context, R.string.timer_running) + "(" + getName() + ")")
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
    }

    private void endTimer() {
        mIsRunning = false;
        sRingtone.play();
        Log.d("end_timer", "Stopping timer...");
        mFinished = true;
        Intent intent = new Intent(context, TimerService.class);
        intent.setAction(TimerService.ACTION_UPDATE_TIMERS);
        context.startForegroundService(intent);
    }

    public int getNotificationId() {
        return TIMER_ID;
    }

}

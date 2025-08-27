package com.app.toolbox.fragment.timer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.ItemView;

final class Timer implements Runnable {
    private final Context context;
    private final ItemView view;
    private final long END_TIME;
    private final int timerID;
    private static final Handler handler=new Handler(Looper.getMainLooper());
    private boolean running = true;
    private long lastNotificationUpdateTime_millis = System.currentTimeMillis();
    private static final int NOTIFICATION_ID=239460000;
    NotificationCompat.Builder time_notification;
    NotificationManager notification_man;

    Timer(Context ctx, ItemView view, long endTime, String name) {
        this.view = view;
        this.END_TIME = endTime;
        context = ctx;
        timerID = hashCode();

        view.setContent(name.isBlank()?context.getString(R.string.unnamed):name);
        view.setOnDeleteListener(v -> {
            Intent intent=getIntent();
            context.sendBroadcast(intent);
            Log.d("action_spoil", "Stop intent sent with ID="+timerID);
        });

        notification_man = context.getSystemService(NotificationManager.class);
        initBasicNotification();
        handler.post(this);
    }

    private Intent getIntent() {
        Intent intent = new Intent(context, StopTimerReceiver.class);
        intent.setAction("STOP_TIMER");
        intent.putExtra("timer_id", timerID);
        return intent;
    }

    private PendingIntent getPendingIntent(Intent intent) {
        return PendingIntent.getBroadcast(context, timerID, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    void terminate() {
        TimerFragment.ringtone.stop();
        notification_man.cancel(timerID);
        running = false;
        handler.removeCallbacks(this);
        removeFromParent(view);
        Toast.makeText(context, ContextCompat.getString(context, R.string.timer_canceled), Toast.LENGTH_SHORT).show();
    }

    private void removeFromParent(View view) {
        LinearLayout parent = (LinearLayout) view.getParent();
        if (parent != null) parent.removeView(view);
    }

    int getTimerID() { return timerID; }
    ItemView getView() { return view; }
    String getName() { return view.getContent(); }

    private void initBasicNotification() {
        time_notification = new NotificationCompat.Builder(context, "timer_channel")
                .setContentTitle(ContextCompat.getString(context, R.string.timer_running)+"("+getName()+")")
                .setContentIntent(Utils.createShowPendingIntent(MainActivity.getFragment(TimerFragment.class), context))
                .setSmallIcon(R.drawable.timer_icon)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setOngoing(true)
                .addAction(R.drawable.delete_icon, ContextCompat.getString(context, R.string.stop), getPendingIntent(getIntent()));
    }

    private void updateNotification() {
        long elapsed_time = END_TIME - System.currentTimeMillis();
        time_notification.setContentText(ContextCompat.getString(context, R.string.time_left) + Utils.longToTime(elapsed_time, false));
        notification_man.notify(timerID, time_notification.build());
    }

    @Override
    public void run() {
        final long CURR_T = System.currentTimeMillis();
        final long ELL_T = END_TIME - CURR_T;
        long SEND_NOTIFICATION_EVERY_MILLIS = 800;
        if (CURR_T - lastNotificationUpdateTime_millis >= SEND_NOTIFICATION_EVERY_MILLIS) {
            updateNotification();
            lastNotificationUpdateTime_millis = CURR_T;
        }
        if (ELL_T < 0 && running) {
            endTimer();
        }
        view.setTitle(Utils.longToTime(ELL_T, true));
        handler.postDelayed(this, 90);
    }

    private void endTimer() {
        running = false;
        TimerFragment.ringtone.play();
        Log.d("action_spoil", "Stopping timer...");
        NotificationCompat.Builder notification_builder=new NotificationCompat.Builder(context, "time_notification")
                .setContentTitle(ContextCompat.getString(context, R.string.time_is_up))
                .setContentText(ContextCompat.getString(context, R.string.timer)+getName()+ContextCompat.getString(context, R.string.ended))
                .setSmallIcon(R.drawable.timer_icon)
                .addAction(R.drawable.delete_icon, ContextCompat.getString(context, R.string.stop), getPendingIntent(getIntent()))
                .setContentIntent(Utils.createShowPendingIntent(MainActivity.getFragment(TimerFragment.class), context));
        notification_man.notify(NOTIFICATION_ID, notification_builder.build());
    }
}

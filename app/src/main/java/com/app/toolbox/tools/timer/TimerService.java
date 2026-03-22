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

/**
 * TimerService is a class that manages {@link Timer} instances.
 * It uses an interface-like system with {@link android.content.BroadcastReceiver}
 * instances to show the updated time of the timers.
 */
public final class TimerService extends Service {
    /**
     * Timer notifications must be silent.
     * This is the silent notification channel ID.
     */
    static final String SILENT_NOTIFICATION = "toolbox.timerService.notificationChannel.silent";

    // Service actions

    /**
     * Action to add a new timer.
     */
    public static final String ACTION_ADD_TIMER  = "toolbox.timerService.actionAddTimer";

    /**
     * Extra to give the name of the timer to get added.
     */
    public static final String TIMER_NAME_EXTRA  = "toolbox.timerService.nameExtra";

    /**
     * Extra to give the duration of the timer to get added.
     */
    public static final String TIME_DELTA_EXTRA  = "toolbox.timerService.deltaExtra";

    /**
     * Action to stop all the timers and kill foreground service.
     */
    public static final String ACTION_STOP_ALL   = "toolbox.timerService.actionStopAll";

    /**
     * Action to stop a single timer and kill the service it this was the only one.
     */
    public static final String ACTION_STOP_TIMER = "toolbox.timerService.actionStopTimer";

    /**
     * Extra to give the id of the timer to stop.
     */
    public static final String TIMER_ID_EXTRA    = "toolbox.timerService.timerIdExtra";

    // Interface actions

    /**
     * Action to send data to another broadcast receiver.
     */
    public static final String ACTION_SEND_DATA  = "toolbox.timerService.sendData";

    /**
     * Extra to put to intent that goes to broadcast receiver.
     */
    public static final String TIMERS_DATA_EXTRA = "toolbox.timerService.timersData";

    /**
     * Builder of the foreground status notification.
     */
    private NotificationCompat.Builder mNotificationBuilder;

    /**
     * List that holds active timers.
     */
    private final List<Timer> mTimers = new ArrayList<>();

    /**
     * Handler used to update timers and notifications.
     */
    private final Handler mTimerHandler = new Handler(Looper.getMainLooper());

    /**
     * Runnable that updates every timer in {@link #mTimers}.
     */
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

    /**
     * Timer update loop. Uses {@link #mTimerUpdater}.
     */
    private final Runnable mTimerUpdateLoop = new Runnable() {
        @Override
        public void run() {
            mTimerUpdater.run();
            mTimerHandler.postDelayed(this, 600);
        }
    };

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     * Initializes stuff.
     */
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

    /**
     * Called by the system every time a client explicitly starts the service by calling
     * {@link android.content.Context#startService}, providing the arguments it supplied and a
     * unique integer token representing the start request.  Do not call this method directly.
     *
     * <p>For backwards compatibility, the default implementation calls
     * {@link #onStart} and returns either {@link #START_STICKY}
     * or {@link #START_STICKY_COMPATIBILITY}.
     *
     * <p class="caution">Note that the system calls this on your
     * service's main thread.  A service's main thread is the same
     * thread where UI operations take place for Activities running in the
     * same process.  You should always avoid stalling the main
     * thread's event loop.  When doing long-running operations,
     * network calls, or heavy disk I/O, you should kick off a new
     * thread, or use {@link android.os.AsyncTask}.</p>
     *
     * @param intent The Intent supplied to {@link android.content.Context#startService},
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to
     * start.  Use with {@link #stopSelfResult(int)}.
     *
     * @return The return value indicates what semantics the system should
     * use for the service's current started state.  It may be one of the
     * constants associated with the {@link #START_CONTINUATION_MASK} bits.
     *
     * @see #stopSelfResult(int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION_ADD_TIMER -> {
                // Stop all timers temporarily
                mTimerHandler.removeCallbacks(mTimerUpdateLoop);

                // Get timer info
                String name = intent.getStringExtra(TIMER_NAME_EXTRA);
                if(name==null||name.equals("null")|| name.isEmpty()) {
                    name = getString(R.string.unnamed);
                }
                long timeDelta = intent.getLongExtra(TIME_DELTA_EXTRA, /*DEFAULT VALUE:*/5000);
                if(timeDelta!=0) {
                    Timer timer = new Timer(getApplicationContext(), timeDelta, name);
                    mTimers.add(timer);
                }

                // startForeground OR stopSelf
                if(!mTimers.isEmpty()/*there are timers left*/) {
                    Notification notification = mNotificationBuilder
                            .setContentTitle(mTimers.size() + getString(R.string.timers_running))
                            .build();
                    startForeground(11, notification);

                    // Restart timers
                    mTimerHandler.post(mTimerUpdateLoop);
                } else {
                    stopSelf();
                }
            }
            case ACTION_STOP_TIMER -> {
                // Stop timers temporarily
                mTimerHandler.removeCallbacks(mTimerUpdateLoop);

                // Get timer info
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

                // startForeground OR stopSelf
                if(!mTimers.isEmpty()/*there are timers left*/) {
                    Log.d("timer_service", "Resending notification...");
                    startForeground(11, mNotificationBuilder
                            .setContentTitle(mTimers.size() + getString(R.string.timers_running))
                            .build());

                    // Restart loop
                    mTimerUpdater.run();
                } else {
                    Log.d("timer_service", "Stopping service...");
                    stopSelf();
                }
            }
            case ACTION_STOP_ALL -> {
                // Stop all timers
                mTimerHandler.removeCallbacks(mTimerUpdateLoop);

                // Destroy all timers
                mTimers.forEach(Timer::destroy);

                // Remove timers from list
                mTimers.clear();

                // Update notification and GUI
                mTimerUpdater.run();

                // Kill foreground service
                stopSelf();
            }
            default -> throw new IllegalIntentContentsException();
        }
        return START_STICKY;
    }

    /**
     * A timer is a {@link Runnable} thing that manages
     * it's notification, ringing, update process.
     */
    private static final class Timer implements Runnable {
        // Android
        private final Context context;

        /**
         * Notification builder of this exact timer's notification.
         */
        private final NotificationCompat.Builder mTimeNotificationBuilder;

        /**
         * Ringtone of this exact timer.
         */
        private final Ringtone mRingtone;

        /**
         * Notification manager instance used to send and cancel notifications.
         */
        private final NotificationManager mNotificationMan;

        // Constants

        /**
         * Milliseconds of time that timer was supposed to stop at.
         * @see #getDelta()
         */
        final long END_TIME;

        /**
         * Id of the timer.
         */
        final int ID;

        /**
         * Title of this timer.
         */
        final String TITLE;

        /**
         * Package private constructor of timer.
         * @param context Context, necessary for ringtone, broadcast etc.
         * @param deltaTime The time timer will run for.
         * @param name      The name of the timer.
         */
        Timer(Context context, long deltaTime, String name) {
            this.context = context;
            ID = hashCode();
            TITLE = name;
            END_TIME = System.currentTimeMillis() + deltaTime;

            // Initialize stuff
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

        /**
         * Destroys resources and frees resources.
         */
        public void destroy() {
            // Stop ringtone to make sure that it doesn't run.
            mRingtone.stop();

            // Cancel the notification.
            mNotificationMan.cancel(ID);

            // Let the user know.
            Toast.makeText(context, context.getString(R.string.timer_canceled), Toast.LENGTH_SHORT).show();
        }

        /**
         * Constructs a notification saying whether the timer is still running or not.
         * @return {@link Notification} with status.
         */
        public Notification createNotification() {
            if (System.currentTimeMillis() - END_TIME > 0/*timer is finished*/) {
                mTimeNotificationBuilder.setContentTitle(context.getString(R.string.time_is_up))
                        .setContentText(context.getString(R.string.timer)+" \""+ TITLE +"\" "+context.getString(R.string.ended));
            } else {
                /*timer is still running*/
                mTimeNotificationBuilder.setContentTitle(context.getString(R.string.timer_running)+" \""+ TITLE +"\"")
                        .setContentText(context.getString(R.string.time_left) + Utils.longToTime(getDelta(), false));
            }
            return mTimeNotificationBuilder.build();
        }

        @Override
        public void run() {
            long delta = System.currentTimeMillis() - END_TIME;
            if (delta > 0) { // timer finished
                if(!mRingtone.isPlaying())
                    mRingtone.play();
            }

            mNotificationMan.notify(ID, createNotification());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // No binding supported.
        return null;
    }
}

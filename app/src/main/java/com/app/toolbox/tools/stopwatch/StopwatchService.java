package com.app.toolbox.tools.stopwatch;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.Utils;

import java.util.Objects;
import java.util.function.Function;

public class StopwatchService extends Service {
    // Service constants
    public static final String ACTION_START_TIMER = "toolbox.stopwatchService.START_TIMER";
    public static final String ACTION_STOP_TIMER  = "toolbox.stopwatchService.STOP_TIMER";
    public static final String ACTION_RESET_TIMER = "toolbox.stopwatchService.RESET_TIMER";

    // Broadcast receiver constants
    public static final String ACTION_SEND_DATA   = "toolbox.stopwatchService.SEND_DATA";

    // Interface constants
    public static final String ACTION_SEND_UPDATE = "toolbox.stopwatchService.sendTime";
    public static final String COUNTED_TIME_EXTRA = "toolbox.stopwatchService.timeExtra";
    public static final String TIMER_STATE_EXTRA  = "toolbox.stopwatchService.stateExtra";

    private static final long TIME_DEFAULT = -1;
    public static final long UNEXISTING_TIME = -2;

    private final Handler mUpdaterHandler = new Handler(Looper.getMainLooper());
    private Builder mTimeNotificationBuilder;
    private boolean mIsRunning = false;

    /**
     * Variable asks the question: <i>When did the timer start for the first time?</i>
     * <b>BUT!</b> When the timer is paused, time 'dead' delta time is added to this variable.
     */
    private long mOnStartTime = TIME_DEFAULT;

    /**
     * Variable asks the question: <i>When did the timer stop last time?</i>
     * That's it. And when the timer gets resumed, it removes the time since <i>then</i> from {@link #mOnStartTime}.
     */
    private long mOnPausedTime = TIME_DEFAULT;

    /**
     * Sends time data to broadcast receivers.
     * Works like an <b>interface</b>, every broadcast receiver can receive the time using the right intent.
     */
    private final Runnable mUpdater = new Runnable() {
        @Override public void run() {
            Intent timeIntent = new Intent(ACTION_SEND_UPDATE).setPackage(getApplicationContext().getPackageName());
            timeIntent.putExtra(COUNTED_TIME_EXTRA, getCountedTime());

            StopwatchState state = StopwatchState.RUNNING;
            if(!mIsRunning) {
                if(mOnStartTime==TIME_DEFAULT) {
                    state = StopwatchState.BEGINNING;
                } else {
                    state = StopwatchState.PAUSED;
                }
            }
            timeIntent.putExtra(TIMER_STATE_EXTRA, (Parcelable)state);
            getApplicationContext().sendBroadcast(timeIntent);
            if (mIsRunning)
                mUpdaterHandler.postDelayed(this, 10);
        }
    };

    /**
     * Sends status notification.
     */
    private final Runnable mNotificationUpdater = new Runnable() {
        @Override public void run() {
            String timeStr = Utils.longToTime(getCountedTime(), false);
            mTimeNotificationBuilder
                    .clearActions()
                    .setContentText(getApplicationContext().getString(R.string.time)+timeStr);
            if (mIsRunning) {
                mTimeNotificationBuilder.addAction(
                        R.drawable.delete_icon,
                        getApplicationContext().getString(R.string.pause),
                        mServiceIntentCreator.apply(ACTION_STOP_TIMER));
            } else {
                mTimeNotificationBuilder.addAction(
                        R.drawable.timer_icon,
                        getApplicationContext().getString(R.string.start),
                        mServiceIntentCreator.apply(ACTION_START_TIMER));
                mTimeNotificationBuilder.addAction(
                        R.drawable.delete_icon,
                        getApplicationContext().getString(R.string.reset),
                        mServiceIntentCreator.apply(ACTION_RESET_TIMER));
            }
            startForeground(1, mTimeNotificationBuilder.build());
            if(mIsRunning)
                mUpdaterHandler.postDelayed(this, 1000);
        }
    };

    /**
     * Creates a pending intent that goes to <b>this service</b> with the given action.
     */
    private final Function<String, PendingIntent> mServiceIntentCreator = action -> PendingIntent.getService(
            getApplicationContext(),
            action.hashCode(),
            new Intent(getApplicationContext(), StopwatchService.class).setAction(action),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
    );

    /**
     * Receives update requests.
     * Receiver is needed; if service is used as receiver, then the service will start unnecessarily.
     */
    private final BroadcastReceiver mUpdateRequestsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendData();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        String action = Objects.requireNonNull(intent.getAction());
        switch (action) {
            case ACTION_RESET_TIMER -> reset();
            case ACTION_START_TIMER -> start();
            case ACTION_STOP_TIMER  -> stop();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimeNotificationBuilder = new Builder(getApplicationContext(), StopwatchRoot.NOTIFICATION_CHANNEL_ID)
                .setSilent(true)
                .setContentTitle(getApplicationContext().getString(R.string.stopwatch_running))
                .setContentIntent(Utils.createShowPagePendingIntent(StopwatchRoot.STRING_ID, getApplicationContext()))
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.stopwatch_icon);
        ContextCompat.registerReceiver(getApplicationContext(), mUpdateRequestsReceiver, new IntentFilter(ACTION_SEND_DATA), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(mUpdateRequestsReceiver);
    }

    private void sendData() {
        mUpdaterHandler.post(mUpdater);
        mUpdaterHandler.post(mNotificationUpdater);
    }

    private void stop() {
        if(!mIsRunning)
            return;
        mIsRunning = false;
        mOnPausedTime = System.currentTimeMillis();

        // Remove callbacks
        mUpdaterHandler.removeCallbacks(mUpdater);
        mUpdaterHandler.removeCallbacks(mNotificationUpdater);

        // Call once to update
        sendData();
    }

    private void start() {
        if(mIsRunning) return;
        mIsRunning = true;
        if(mOnStartTime != TIME_DEFAULT && mOnPausedTime != TIME_DEFAULT) { // If was just paused.
            mOnStartTime += System.currentTimeMillis() - mOnPausedTime;
        } else { // If starts for the first time.
            mOnStartTime = System.currentTimeMillis();
        }
        mOnPausedTime = TIME_DEFAULT;
        sendData();
    }

    private void reset() {
        if(mIsRunning) stop();

        mOnStartTime = TIME_DEFAULT;
        mOnPausedTime = TIME_DEFAULT;

        // Call once to update
        mUpdaterHandler.post(mUpdater);
        mUpdaterHandler.post(mNotificationUpdater);

        // Stop self because not used
        stopSelf();
    }

    private long getCountedTime() {
        if(mOnStartTime == TIME_DEFAULT)
            return 0;
        long pausedDelta = mOnPausedTime!=TIME_DEFAULT
                ?System.currentTimeMillis()-mOnPausedTime
                :0;
        Log.d("stopwatch_s", "Paused Delta: "+Utils.longToTime(pausedDelta, true));
        return System.currentTimeMillis() - mOnStartTime - pausedDelta;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

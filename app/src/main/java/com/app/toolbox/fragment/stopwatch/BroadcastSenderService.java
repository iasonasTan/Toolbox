package com.app.toolbox.fragment.stopwatch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.app.toolbox.utils.IntentContentsMissingException;

import java.util.Objects;

public final class BroadcastSenderService extends Service {
    static final String ACTION_STOP  = "toolbox.broadcastSenderService.STOP";
    static final String ACTION_START = "toolbox.broadcastSenderService.START";
    static final String ACTION_RESET = "toolbox.broadcastSenderService.RESET";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            // android is testing, or logic error
            return START_STICKY;

        Intent serviceIntent = new Intent(getApplicationContext(), StopwatchService.class);
        switch (Objects.requireNonNull(intent.getAction())) {
            case ACTION_STOP:
                serviceIntent.setAction(StopwatchService.ACTION_STOP_TIMER);
                break;
            case ACTION_START:
                serviceIntent.setAction(StopwatchService.ACTION_START_TIMER);
                break;
            case ACTION_RESET:
                serviceIntent.setAction(StopwatchService.ACTION_RESET_TIMER);
                break;
            default:
                throw new IntentContentsMissingException();
        }
        getApplicationContext().startForegroundService(serviceIntent);
        sendBroadcast(new Intent(intent.getAction()).setPackage(getPackageName()));

        return START_NOT_STICKY;
    }
}

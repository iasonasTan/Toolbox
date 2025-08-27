package com.app.toolbox.fragment.stopwatch;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Objects;

public final class BroadcastSenderService extends Service {
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

        Intent serviceIntent = new Intent(getApplicationContext(), TimerService.class);
        switch (Objects.requireNonNull(intent.getAction())) {
            case "STOP":
                serviceIntent.setAction("STOP_TIMER");
                break;
            case "START":
                serviceIntent.setAction("START_TIMER");
                break;
            case "RESET":
                serviceIntent.setAction("RESET_TIMER");
                break;
            default:
                throw new IllegalArgumentException();
        }
        getApplicationContext().startForegroundService(serviceIntent);
        sendBroadcast(new Intent(intent.getAction()).setPackage(getPackageName()));

        return START_NOT_STICKY;
    }
}

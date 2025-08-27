package com.app.toolbox.fragment.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Iterator;
import java.util.NoSuchElementException;

// ALWAYS PUBLIC AND REGISTERED IN MANIFEST
public final class StopTimerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final int REQUIRED_ID = intent.getIntExtra("timer_id", 0);
        Log.d("action_spoil", "searching for timer with ID="+REQUIRED_ID);
        Iterator<Timer> iter=TimerManager.instance.iterator();
        while(iter.hasNext()) {
            Timer timer=iter.next();
            int currentID = timer.getTimerID();
            if (currentID == REQUIRED_ID) {
                Log.d("action_spoil", "timer "+currentID+" killed!");
                timer.terminate();
                iter.remove();
                return;
            }
         }
        throw new NoSuchElementException("cannot find element with ID="+REQUIRED_ID);
    }
}

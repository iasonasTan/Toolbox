package com.app.toolbox.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.app.toolbox.MainActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@SuppressWarnings("unused")
public final class Utils {
    private Utils(){}

    /**
     * Method that converts milliseconds as long to formated string.
     *
     * @param millis         input millis
     * @param millisOnFormat the string format will contain milliseconds in the end.
     * @return time format: HH:MM:SS:MM (hours:minutes:seconds:milliseconds)
     */
    public static String longToTime(long millis, boolean millisOnFormat) {
        final boolean INPUT_POSITIVE = millis >= 0;
        millis = Math.abs(millis);
        long secs = 0, mins = 0, hours = 0;
        while (millis >= 1000) {
            secs += 1;
            millis -= 1000;
        }
        while (secs >= 60) {
            mins += 1;
            secs -= 60;
        }
        while (mins >= 60) {
            hours += 1;
            mins -= 60;
        }
        millis /= 10;
        String timeFormated = String.format(Locale.ENGLISH, "%s%02d:%02d:%02d",
                INPUT_POSITIVE ? "" : "-", hours, mins, secs);
        return millisOnFormat ? timeFormated + "." + millis : timeFormated;
    }

    /**
     * Method that gives a {@link PendingIntent} which starts {@link MainActivity} and a {@link ToolFragment}
     *
     * @param fragToShow the fragment you want to show
     * @return {@link PendingIntent} which starts activity and shows the fragment passed
     */
    public static PendingIntent createShowPendingIntent(ToolFragment fragToShow, Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(fragToShow.name());
        intent.putExtra("FRAGMENT_NAME", fragToShow.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * creates a pending intent that triggers the method onReceive(Context) in given implementation
     * @param context The Context in which this PendingIntent should perform the action.
     * @param receiver Receiver to trigger when pending intent is send.
     * @param action String to specify the action.
     * @return Returns a new {@link PendingIntent}
     */
    public static PendingIntent createActionPendingIntent(Context context, I_BroadcastReceiver receiver, String action) {
        Intent intent = new Intent(context, PrivateReceiver.class);
        intent.setAction(action);
        receivers_map.put(action, receiver);
        return PendingIntent.getBroadcast(context, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * {@link BroadcastReceiver} as an Interface.
     * Made for use with {@link #createActionPendingIntent(Context, I_BroadcastReceiver, String)}
     */
    public interface I_BroadcastReceiver {
        void onReceive(Context context);
    }

    /**
     * Map to store receivers and IDs
     */
    private static final Map<String, I_BroadcastReceiver> receivers_map = new HashMap<>();

    /**
     * Responsible for sending the broadcast to the right intent
     * Listens to broadcasts and sends then to the right I_BroadcastReceivers
     * Must be included to AndroidManifest.xml to work
     */
    public static final class PrivateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if(receivers_map.containsKey(action))
                // noinspection all
                receivers_map.get(action).onReceive(context);
            else
                throw new NoSuchElementException(action+" not found");
        }
    }

}

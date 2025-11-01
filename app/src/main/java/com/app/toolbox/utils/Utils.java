package com.app.toolbox.utils;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.app.toolbox.MainActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

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
    public static String longToTime(final long millis, final boolean millisOnFormat) {
        long mut_millis = Math.abs(millis);
        long hours = mut_millis / (1000 * 60 * 60);
        long mins = (mut_millis / (1000 * 60)) % 60;
        long secs = (mut_millis / 1000) % 60;
        long ms = mut_millis % 1000;

        String sign = millis >= 0 ? "" : "-";
        String timeFormatted = String.format(Locale.getDefault(), "%s%02d:%02d:%02d", sign, hours, mins, secs);

        return millisOnFormat
                ? String.format(Locale.getDefault(), "%s.%03d", timeFormatted, ms)
                : timeFormatted;
    }

    /**
     * Method that gives a {@link PendingIntent} which starts {@link MainActivity} and a {@link ToolFragment}
     *
     * @param pageID the fragment you want to show
     * @return {@link PendingIntent} which starts activity and shows the fragment passed
     */
    public static PendingIntent createShowPagePendingIntent(String pageID, Context context) {
        Log.d("pIntentCreation", "Creating pending intent showing fragment: "+pageID);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_PAGE);
        intent.putExtra(MainActivity.PAGE_NAME_EXTRA, pageID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, pageID.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
    }

    public static void checkIntent(@NonNull Intent intent, String... extras) {
        Objects.requireNonNull(intent);
        for (String extra : extras) {
            if(!intent.hasExtra(extra))
                throw new IntentContentsMissingException();
        }
        if(intent.getAction()==null) {
            throw new IntentContentsMissingException();
        }
    }

    /**
     * creates a pending intent that triggers the method onReceive(Context) in given implementation
     * @param context The Context in which this PendingIntent should perform the action.
     * @param receiver Receiver to trigger when pending intent is send.
     * @param action String to specify the action.
     * @return Returns a new {@link PendingIntent}
     */
    @Deprecated(forRemoval = true)
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
    @Deprecated(forRemoval = true)
    public interface I_BroadcastReceiver {
        void onReceive(Context context);
    }

    /**
     * Map to store receivers and IDs
     */
    @Deprecated(forRemoval = true)
    private static final Map<String, I_BroadcastReceiver> receivers_map = new HashMap<>();

    /**
     * Responsible for sending the broadcast to the right intent
     * Listens to broadcasts and sends then to the right I_BroadcastReceivers
     * Must be included to AndroidManifest.xml to work
     */
    @Deprecated(forRemoval = true)
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

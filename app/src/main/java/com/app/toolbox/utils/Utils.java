package com.app.toolbox.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.app.toolbox.MainActivity;

import java.util.Locale;
import java.util.Objects;

public final class Utils {
    /**
     * Private constructor to prevent instantiation.
     */
    private Utils(){}

    /**
     * Converts milliseconds to formated string.
     *
     * @param millis         millis to convert to formated string.
     * @param millisOnFormat if {@code true} returned format will contain milliseconds.
     * @return {@code millis} formated as: HH:MM:SS.mm or HH:MM:SS
     */
    public static String longToTime(final long millis, boolean millisOnFormat) {
        long mut_millis = Math.abs(millis);
        long hours = mut_millis / (1000 * 60 * 60);
        long mins = (mut_millis / (1000 * 60)) % 60;
        long secs = (mut_millis / 1000) % 60;
        long ms = mut_millis % 1000;

        String sign = millis >= 0 ? "" : "-";
        String timeFormatted = String.format(Locale.getDefault(), "%s%02d:%02d:%02d", sign, hours, mins, secs);
        return millisOnFormat ? String.format(Locale.getDefault(), "%s.%03d", timeFormatted, ms) : timeFormatted;
    }

    /**
     * Creates a {@link PendingIntent} which shows {@link MainActivity} and automatically tell activity to switch
     * to a {@link PageFragment} that has the given id.
     *
     * @param pageID  id of fragment you want to show
     * @return {@link PendingIntent} that shows {@link MainActivity} and a {@link PageFragment}.
     */
    public static PendingIntent createShowPagePendingIntent(@NonNull String pageID, Context context) {
        Log.d("pIntentCreation", "Creating pending intent showing fragment: "+pageID);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_PAGE);
        intent.putExtra(MainActivity.PAGE_NAME_EXTRA, pageID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, pageID.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * Checks if {@link Intent} contains <i>non-null</i> action and extras with given names.
     * <b>Extras</b> can be null, method only checks that they exist.
     *
     * @param intent {@code Intent} to check.
     * @param extras {@code Extras} that the intent must include.
     */
    public static void checkIntent(@NonNull Intent intent, @NonNull String... extras) {
        Objects.requireNonNull(intent);
        for (String extra : extras) {
            if(!intent.hasExtra(extra))
                throw new IntentContentsMissingException();
        }
        if(intent.getAction()==null) {
            throw new IntentContentsMissingException();
        }
    }

}

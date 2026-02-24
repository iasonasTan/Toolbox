package com.app.toolbox.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.app.toolbox.MainActivity;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import yuku.ambilwarna.AmbilWarnaDialog;

@SuppressWarnings("unused")
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
    @Deprecated
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

    /**
     * Checks if given array contains given object.
     * @param array array to check it's elements
     * @param obj object that the array may contains
     * @return returns {@code true} if given array contains given object;
     * {@code false} otherwise.
     */
    public static boolean arrayContains(Object[] array, Object obj) {
        for (Object o : array) {
            if(o.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculates factorial of given number.
     * @param n number to get it's factorial
     * @return factorial of given number n
     */
    public static BigInteger factorial (double n) {
        BigInteger bigInteger=BigInteger.ONE;
        for (long i = 2; i <= n; i++) {
            bigInteger = bigInteger.multiply(BigInteger.valueOf(i));
        }
        return bigInteger;
    }

    /**
     * Shows a color picker and executes taken code.
     * @param onCancel     {@link Runnable} to execute when dialog is ignored.
     * @param initialColor Color to start the picker from.
     * @param onOk         {@link Consumer<Integer>} executes when a color is selected.
     * @param context      Context used to show the dialog.
     */
    public static void showColorPicker(Context context, int initialColor, Consumer<Integer> onOk, Runnable onCancel) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(context, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override public void onCancel(AmbilWarnaDialog dialog) {
                onCancel.run();
            }
            @Override public void onOk(AmbilWarnaDialog dialog, int color) {
                onOk.accept(color);
            }
        });
        dialog.show();
    }

    /**
     * Converts dp to px.
     * @param context Application context.
     * @param dp      Size {@code DP} to convert to Size {@code PX}.
     * @return        returns Size {@code PX} as integer.
     */
    public static int dpToPx(Context context, int dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }

    /**
     * Tries to execute given runnable.
     * @param runnable Runnable to try to execute.
     * @return Returns {@code false} if any exception is thrown; {@code true} otherwise.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean execute(Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Exception e) {
            Log.d("execute", "Exception "+e);
            return false;
        }
    }

    /**
     * Converts boolean to int based on static visibility constants of class {@link View}.
     * @param visible Boolean representing if the view is currently visible of hidden.
     * @return {@link View#VISIBLE} if view is visible; {@link View#GONE} otherwise.
     */
    public static int booleanVisibility(boolean visible) {
        return visible ? View.VISIBLE : View.GONE;
    }

    /**
     * Creates an {@link IntentFilter} with given actions.
     * @param actions Actions to add to intent filter.
     * @return Intent filter with given actions.
     */
    public static IntentFilter intentFilter(String... actions) {
        IntentFilter filter = new IntentFilter();
        for (String action: actions)
            filter.addAction(action);
        return filter;
    }
}

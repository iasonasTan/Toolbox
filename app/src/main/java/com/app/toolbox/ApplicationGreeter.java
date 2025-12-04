package com.app.toolbox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Date;

/**
 * Greets user and suggests them to give app some permissions.
 * Stores when it was the last time the class greeted.
 * Shows {@link DialogInterface} to ask user for permissions.
 */

public final class ApplicationGreeter {
    private final Context context;
    private static final String GREETER_PREFS_NAME = "toolbox.greeter.PREFERENCES";
    private static final String GREET_STATUS       = "toolbox.greeter.key.greetShown";
    private static final String GREET_NEVER        = "toolbox.greeter.value.shown";
    private static final String GREET_AT_DATE      = "toolbox.greeter.value.lastPostponed";
    private static final long DAY_MILLIS           = 86_400_000;

    public ApplicationGreeter(Context context1) {
        this.context = context1;
    }

    public void greet() {
        SharedPreferences preferences = context.getSharedPreferences(GREETER_PREFS_NAME, Context.MODE_PRIVATE);
        String greetStatus = preferences.getString(GREET_STATUS, GREET_AT_DATE);
        switch(greetStatus) {
            case GREET_NEVER:
                // nothing
                break;
            case GREET_AT_DATE:
                long greetDateMillis = preferences.getLong(GREET_AT_DATE, 0);
                Date greetDate = new Date(greetDateMillis);
                Date now = new Date(System.currentTimeMillis());
                if(now.after(greetDate)) {
                    showGreet();
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void showGreet() {
        if (context.getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(context.getPackageName())) {
            context.getSharedPreferences(GREETER_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(GREET_STATUS, GREET_NEVER)
                    .apply();
            return;
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.permission_helps)
                .setMessage(context.getString(R.string.battery_warning))
                .setNegativeButton(R.string.never_ask_again, createNegativeListener())
                .setPositiveButton(R.string.lets_do_it, createPositiveListener())
                .setNeutralButton(R.string.remind_me_later, createNeutralListener())
                .setCancelable(false)
                .show();
    }

    private DialogInterface.OnClickListener createNeutralListener() {
        return (dialog, which) -> {
            long nextGreetDate = System.currentTimeMillis()+DAY_MILLIS;
            context.getSharedPreferences(GREETER_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(GREET_AT_DATE, nextGreetDate)
                    .apply();
            dialog.dismiss();
        };
    }

    private DialogInterface.OnClickListener createPositiveListener() {
        return (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(intent);
            dialog.dismiss();
        };
    }

    private DialogInterface.OnClickListener createNegativeListener() {
        return (dialog, which) -> {
            context.getSharedPreferences(GREETER_PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(GREET_STATUS, GREET_NEVER)
                    .apply();
            dialog.dismiss();
        };
    }
}

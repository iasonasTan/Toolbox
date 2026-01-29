package com.app.toolbox.tools.randnumgen.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.content.ContextCompat;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.SettingsActivity;
import com.app.toolbox.tools.randnumgen.RandNumGenFragment;

import java.util.Locale;
import java.util.Objects;

public class RandNumGenWidget extends AppWidgetProvider {
    public static final String ACTION_GENERATE_NUMBER  = "toolbox.rng.widget.generateNumber";
    public static final String WIDGET_ID_EXTRA         = "toolbox.rng.widget.widgetId";
    public static final float  DEFAULT_LIMIT           = 100;

    private float getLimit(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(SettingsActivity.RNG_WIDGET_LIMIT_EXTRA, DEFAULT_LIMIT);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        super.onUpdate(context, manager, ids);
        for (int id : ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.rng_widget);
            views.setTextViewText(R.id.limit_tv, ContextCompat.getString(context, R.string.limit)+getLimit(context));
            views.setOnClickPendingIntent(R.id.generate_button, createGeneratePendingIntent(context, id));
            views.setOnClickPendingIntent(R.id.number_tv, createShowRNGPendingIntent(context));
            manager.updateAppWidget(id, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.rng_widget);
        ComponentName cName = new ComponentName(context, RandNumGenWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        String action = Objects.requireNonNull(intent.getAction());
        Log.d("widget_debug", action);
        switch (action) {
            case ACTION_GENERATE_NUMBER -> {
                float limit = getLimit(context);
                views.setTextViewText(R.id.limit_tv, ContextCompat.getString(context, R.string.limit) + limit);
                int id = intent.getIntExtra(WIDGET_ID_EXTRA, 0); // id value

                float num = (float) (Math.random() * limit);
                Log.d("widget_debug", "Number: " + num + ", Limit: " + limit);
                views.setTextViewText(R.id.number_tv, String.format(Locale.ENGLISH, "%.2f", num));
                manager.updateAppWidget(id, views);
            }
            case "android.appwidget.action.APPWIDGET_UPDATE" -> {
                float limit = getLimit(context);
                views.setTextViewText(R.id.limit_tv, ContextCompat.getString(context, R.string.limit) + limit);
                int[] ids = manager.getAppWidgetIds(cName);
                manager.updateAppWidget(ids, views);
            }
        }
    }

    private PendingIntent createGeneratePendingIntent(Context context, int id) {
        Intent generateIntent = new Intent(context, RandNumGenWidget.class)
                .putExtra(WIDGET_ID_EXTRA, id)
                .setAction(ACTION_GENERATE_NUMBER);
        return PendingIntent.getBroadcast(context, 2000+id, generateIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent createShowRNGPendingIntent(Context context) {
        Intent showRNGIntent = new Intent(context, MainActivity.class).setAction(MainActivity.SWITCH_PAGE);
        showRNGIntent.putExtra(MainActivity.PAGE_NAME_EXTRA, RandNumGenFragment.STRING_ID);
        return PendingIntent.getActivity(context, 3000, showRNGIntent, PendingIntent.FLAG_IMMUTABLE);
    }
}

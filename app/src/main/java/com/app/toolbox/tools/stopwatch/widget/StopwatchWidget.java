package com.app.toolbox.tools.stopwatch.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.tools.stopwatch.StopwatchService;
import com.app.toolbox.utils.Utils;

public final class StopwatchWidget extends AppWidgetProvider {
    public static final String TIME_EXTRA = "toolbox.stopwatch.widget.TIME_EXTRA";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for(int id: ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stopwatch_widget);

            views.setOnClickPendingIntent(R.id.start_button, timerIntent(StopwatchService.ACTION_START_TIMER, context));
            views.setOnClickPendingIntent(R.id.stop_button, timerIntent(StopwatchService.ACTION_STOP_TIMER, context));
            views.setOnClickPendingIntent(R.id.reset_button, timerIntent(StopwatchService.ACTION_RESET_TIMER, context));
            views.setOnClickPendingIntent(R.id.time_view, startIntent(context));

            manager.updateAppWidget(id, views);
        }
    }

    private PendingIntent startIntent(Context context) {
        String action = "toolbox.mainActivity.startStopwatch";
        Intent startTimerIntent = new Intent(context, MainActivity.class);
        startTimerIntent.setAction(action);
        return PendingIntent.getActivity(context, action.hashCode(), startTimerIntent, PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent==null||!intent.hasExtra(TIME_EXTRA))
            return;

        final long TIME_MILLIS = intent.getLongExtra(TIME_EXTRA, 1000);
        final String TIME_TEXT = Utils.longToTime(TIME_MILLIS, false);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stopwatch_widget);
        views.setTextViewText(R.id.time_view, TIME_TEXT);

        ComponentName cName = new ComponentName(context, StopwatchWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] widgetIds = manager.getAppWidgetIds(cName);

        manager.updateAppWidget(widgetIds, views);
    }

    private PendingIntent timerIntent(String action, Context context) {
        Intent serviceIntent = new Intent(context, StopwatchService.class);
        serviceIntent.setAction(action);
        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        return PendingIntent.getService(context, action.hashCode(), serviceIntent, flags);
    }
}

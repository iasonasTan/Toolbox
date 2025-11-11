package com.app.toolbox.tools.stopwatch.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;

public class StopwatchWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for(int id: ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stopwatch_widget);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction("toolbox.mainActivity.startStopwatch");
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 738, intent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.main, pendingIntent);

            manager.updateAppWidget(id, views);
        }
    }
}

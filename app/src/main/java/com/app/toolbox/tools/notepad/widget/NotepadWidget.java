package com.app.toolbox.tools.notepad.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;

/**
 * Not enabled yet.
 */
@SuppressWarnings("all") // Allowed because class is unused.
public final class NotepadWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notepad_widget);

            views.setOnClickPendingIntent(R.id.open_button, createOnOpenPIntent(context, "Test"));
            views.setTextViewText(R.id.title_tv, "a");
            views.setTextViewText(R.id.desc_tv, "b");

            manager.updateAppWidget(id, views);
        }
    }

    private PendingIntent createOnOpenPIntent(Context context, String noteName) {
        String action = "toolbox.mainActivity.openNote";
        Intent openIntent = new Intent(context, MainActivity.class)
                .setAction(action)
                .putExtra("noteName", noteName);
        return PendingIntent.getActivity(context, action.hashCode(), openIntent, PendingIntent.FLAG_IMMUTABLE);
    }
}

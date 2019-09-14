package com.sandra.calendearlife.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.R
import com.sandra.calendearlife.home.HomeFragment

/**
 * Implementation of App Widget functionality.
 */
class RemindersWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

            val widgetText = context.getString(R.string.appwidget_text)
            // Construct the RemoteViews object

            val views = RemoteViews(context.packageName, R.layout.reminder_widget)

            val serviceIntent = Intent(context, ReminderWidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

            views.setRemoteAdapter(R.id.remindersWidgetStackView, serviceIntent)
            views.setEmptyView(R.id.remindersWidgetStackView, R.id.remindersTextView)



            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}


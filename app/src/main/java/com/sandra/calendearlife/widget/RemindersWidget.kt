package com.sandra.calendearlife.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Reminders


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

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if ("click" == intent?.action) {
            val remindersItem = intent.getParcelableExtra<Reminders>("remindersItem")
            Log.d("sandraaa", "remindersItem = $remindersItem")
        }
    }

    companion object {

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.reminder_widget)

            val serviceIntent = Intent(context, ReminderWidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

            views.setRemoteAdapter(R.id.remindersWidgetStackView, serviceIntent)

            views.setOnClickPendingIntent(R.id.title, getPendingIntent(context))



            val clickIntent = Intent(context, RemindersWidget::class.java)
            clickIntent.action = "click"
            val clickPendingIntent = PendingIntent.getBroadcast(
                context,
                0, clickIntent, 0
            )

            views.setPendingIntentTemplate(R.id.remindersWidgetStackView, clickPendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)


        }
        private fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(context, 12345, intent, 0)
        }
    }
}


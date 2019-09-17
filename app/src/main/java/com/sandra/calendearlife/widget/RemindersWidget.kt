package com.sandra.calendearlife.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.RemoteViews
import android.widget.Toast
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.reminders.RemindersFragment
import kotlinx.coroutines.selects.select


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

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == "click") {

            Log.d("sandraaa", "extra = ${intent.extras}")

            if (intent.hasExtra("remindersItem")) {

                val remindersItem = intent.extras?.getString("remindersItem")
                Log.d("sandraaa", "remindersItem = $remindersItem")
                executeResumeAction(context, intent)

            } else if (intent.hasExtra("position")) {

                val appwidgetId = intent.extras!!.getInt("refreshId")
                selectedPostion = intent.extras!!.getInt("position")

                AppWidgetManager.getInstance(context)
                    .notifyAppWidgetViewDataChanged(appwidgetId, R.id.remindersWidgetStackView)

            }
        }
    }

    companion object {

        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.reminder_widget)
            views.setOnClickPendingIntent(R.id.remindAdd, getPendingIntent(context))

            Log.d("sandraaa", "appwidgetId = $appWidgetId")

            //set first login
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            intent.putExtra("turn", "login")
            views.setOnClickPendingIntent(R.id.reminderWidget, pendingIntent)

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    "login",
                    false
                )
            ) {

                val serviceIntent = Intent(context, ReminderWidgetService::class.java)
                serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

                val clickIntent = Intent(context, RemindersWidget::class.java)
                clickIntent.action = "click"

                val clickPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0, clickIntent, 0
                )

                views.setRemoteAdapter(R.id.remindersWidgetStackView, serviceIntent)
                views.setPendingIntentTemplate(R.id.remindersWidgetStackView, clickPendingIntent)

                views.setViewVisibility(R.id.remindersWidgetStackView, View.VISIBLE)
                views.setViewVisibility(R.id.empty, View.GONE)

            } else {
                views.setViewVisibility(R.id.remindersWidgetStackView, View.GONE)
                views.setViewVisibility(R.id.empty, View.VISIBLE)
            }

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun executeResumeAction(context: Context, intent: Intent?) {

            val bundle = intent?.getStringExtra("remindersItem")
            val launchActivityIntent = MainActivity().createFlagIntent(context, bundle, Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(launchActivityIntent)
        }

        private fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("turn", "addFragment")
            return PendingIntent.getActivity(context, 12345, intent, 0)
        }

        var selectedPostion: Int = -1
    }
}


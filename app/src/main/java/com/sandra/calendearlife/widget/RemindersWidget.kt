package com.sandra.calendearlife.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.PreferenceManager
import android.view.View
import android.widget.RemoteViews
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.SharedPreferenceKey
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.ADDFRAGMENT
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CLICK
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.LOGIN
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.POSITION
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.REMINDERSITEM
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.TURN

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

        if (intent?.action == CLICK) {

            if (intent.hasExtra(REMINDERSITEM)) {
                intent.extras?.getString(REMINDERSITEM)
                executeResumeAction(context, intent)

            } else if (intent.hasExtra(POSITION)) {

                val appwidgetId = intent.extras!!.getInt(SharedPreferenceKey.REFRESHID)
                selectedPosition = intent.extras!!.getInt(POSITION)
                AppWidgetManager.getInstance(context)
                    .notifyAppWidgetViewDataChanged(appwidgetId, R.id.remindersWidgetStackView)

            }
        }
    }

    companion object {

    private fun updateAppWidget(
        context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.reminders_widget)
        views.setOnClickPendingIntent(
            R.id.remindAdd,
            getPendingIntent(context)
        )

        //set first login
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        intent.putExtra(TURN, LOGIN)
        views.setOnClickPendingIntent(R.id.reminderWidget, pendingIntent)

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                LOGIN,
                false
            )
        ) {

            val serviceIntent = Intent(context, RemindersWidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

            val clickIntent = Intent(context, RemindersWidget::class.java)
            clickIntent.action = CLICK

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
            views.setViewVisibility(R.id.remindAdd, View.INVISIBLE)
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun executeResumeAction(context: Context, intent: Intent?) {

        val bundle = intent?.getStringExtra(REMINDERSITEM)
        val launchActivityIntent = MainActivity().createFlagIntent(context, bundle, Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(launchActivityIntent)
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(TURN, ADDFRAGMENT)
        return PendingIntent.getActivity(context, 12345, intent, 0)
    }

    var selectedPosition: Int = -1
    }
}


package com.sandra.calendearlife.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.widget.RemindersWidget.Companion.selectedPostion
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ReminderWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetItemFactory(MyApplication.instance, intent!!)
    }

    class WidgetItemFactory(context: Context, intent: Intent) : RemoteViewsFactory {
        var db = FirebaseFirestore.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
        val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

        private var context = context

        lateinit var remindAdd: Reminders
        val remindersItem = ArrayList<Reminders>()

        private var appWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID
            , AppWidgetManager.INVALID_APPWIDGET_ID)




        override fun onCreate() {

            val appWidgetManager = AppWidgetManager.getInstance(context)


            db.collection("data")
                .document(UserManager.id!!)
                .collection("calendar")
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {
                        Log.d("widgetCalendar", "${calendar.id} => ${calendar.data}")

                        //get reminders ( only ischecked is false )
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .document(calendar.id)
                            .collection("reminders")
                            .whereEqualTo("isChecked", false)
                            .get()
                            .addOnSuccessListener { documents ->

                                for (reminder in documents) {
                                    Log.d("widgetReminder", "${reminder.id} => ${reminder.data}")

                                    val setDate = (reminder.data["setDate"] as Timestamp)
                                    val remindDate = (reminder.data["remindDate"] as Timestamp)

                                    remindAdd = Reminders(
                                        simpleDateFormat.format(setDate.seconds * 1000),
                                        reminder.data["title"].toString(),
                                        reminder.data["setRemindDate"].toString().toBoolean(),
                                        simpleDateFormat.format(remindDate.seconds * 1000),
                                        reminder.data["remindDate"] as Timestamp,
                                        reminder.data["isChecked"].toString().toBoolean(),
                                        reminder.data["note"].toString(),
                                        reminder.data["frequency"].toString(),
                                        reminder.data["documentID"].toString()
                                    )
                                    remindersItem.add(remindAdd)
                                }
                                Log.d("sandraaa", "remindersItem=  $remindersItem")

                                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.remindersWidgetStackView)
                            }
                    }
                }

        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onDataSetChanged() {
            Log.d("sandraaa", "data change")
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {

            val views = RemoteViews(context.packageName, R.layout.item_reminder_widget)

            views.setTextViewText(R.id.remindersTextView, remindersItem[position].title)

            if (remindersItem[position].setRemindDate){
            views.setTextViewText(R.id.remindersTime, remindersItem[position].remindDate)}
            else {
                views.setViewVisibility(R.id.remindersTime, View.GONE)
            }

            if (remindersItem[position].remindTimestamp.seconds < Timestamp.now().seconds){
                views.setTextColor(R.id.remindersTime,Color.parseColor("#f44336"))
            }

            val fillIntent = Intent()
            fillIntent.putExtra("remindersItem", remindersItem[position].documentID)

            val buttonIntent = Intent()
            buttonIntent.putExtra("position", position)

            views.setOnClickFillInIntent(R.id.remindersCheckedButton, fillIntent.addCategory("remindersItem"))

            views.setOnClickFillInIntent(R.id.remindersTextView, fillIntent.addCategory("position"))


            if (position == selectedPostion) {
                views.setViewVisibility(R.id.remindersCheckedStauts, View.VISIBLE)
                Log.d("sandraaa", "position = $position, selectedPosition = $selectedPostion")
            } else {
                views.setViewVisibility(R.id.remindersCheckedStauts, View.GONE)
                Log.d("sandraaa", "position = $position, selectedPosition = $selectedPostion")
            }

            return views
        }

        override fun getCount(): Int {
            return remindersItem.size
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun onDestroy() {

        }
    }
}
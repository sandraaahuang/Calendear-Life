package com.sandra.calendearlife.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager
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

        private var context: Context = context

        lateinit var remindAdd: Reminders
        val remindersItem = ArrayList<Reminders>()
        val _liveReminders = MutableLiveData<List<Reminders>>()
        val liveReminders: LiveData<List<Reminders>>
            get() = _liveReminders

        private var appWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID
            , AppWidgetManager.INVALID_APPWIDGET_ID)

        init {
            getRemindersItem()
        }

        private fun getRemindersItem(){
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
                                _liveReminders.value = remindersItem
                                Log.d("sandraaa", "liveDate=  ${liveReminders.value}")
                            }
                    }
                }
        }

        override fun onCreate() {

        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun onDataSetChanged() {
            Log.d("sandraaa", "why need to have something here")
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.reminder_widget)
            views.setTextViewText(R.id.remindersTextView, liveReminders.value!![position].title)
            Log.d("sandraaa", "remindersItem[position].title = ${liveReminders.value!![position].title}")
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
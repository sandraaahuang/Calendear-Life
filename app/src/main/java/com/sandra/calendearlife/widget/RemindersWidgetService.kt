package com.sandra.calendearlife.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.Const
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ISCHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETREMINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.POSITION
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.REFRESHID
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.REMINDERSITEM
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.widget.RemindersWidget.Companion.selectedPosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RemindersWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetItemFactory(MyApplication.instance, intent!!)
    }

    class WidgetItemFactory(private val context: Context, intent: Intent) : RemoteViewsFactory {
        var db = FirebaseFirestore.getInstance()

        val locale: Locale =
            if (Locale.getDefault().toString() == CHINESE) {
                Locale.TAIWAN
            } else {
                Locale.ENGLISH
            }
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

        lateinit var remindAdd: Reminders
        private val remindersItem = ArrayList<Reminders>()

        private var appWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID
            , AppWidgetManager.INVALID_APPWIDGET_ID)

        override fun onCreate() {

            val appWidgetManager = AppWidgetManager.getInstance(context)

            db.collection(DATA)
                .document(UserManager.id!!)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        //get reminders ( only ischecked is false )
                        db.collection(DATA)
                            .document(UserManager.id!!)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(ISCHECKED, false)
                            .get()
                            .addOnSuccessListener { documents ->

                                for (reminder in documents) {

                                    val setDate = (reminder.data[SETDATE] as Timestamp)
                                    val remindDate = (reminder.data[REMINDDATE] as Timestamp)

                                    remindAdd = Reminders(
                                        simpleDateFormat.format(setDate.seconds * 1000),
                                        reminder.data[TITLE].toString(),
                                        reminder.data[SETREMINDATE].toString().toBoolean(),
                                        simpleDateFormat.format(remindDate.seconds * 1000),
                                        reminder.data[REMINDDATE] as Timestamp,
                                        reminder.data[ISCHECKED].toString().toBoolean(),
                                        reminder.data[NOTE].toString(),
                                        reminder.data[FREQUENCY].toString(),
                                        reminder.data[DOCUMENTID].toString()
                                    )
                                    remindersItem.add(remindAdd)
                                }

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
            Logger.d("data change")
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun getViewAt(position: Int): RemoteViews {

            val views = RemoteViews(context.packageName, R.layout.item_reminders_widget)

            views.setTextViewText(R.id.remindersTextView, remindersItem[position].title)

            if (remindersItem[position].setRemindDate){
            views.setTextViewText(R.id.remindersTime, remindersItem[position].remindDate)}
            else {
                views.setViewVisibility(R.id.remindersTime, View.GONE)
            }

            if (remindersItem[position].remindTimestamp.seconds < Timestamp.now().seconds){
                views.setTextColor(R.id.remindersTime, MyApplication.instance.getColor(R.color.delete_red))
            }

            val positionIntent = Intent().apply {
                putExtra(POSITION, position)
                putExtra(REFRESHID, appWidgetId)
            }

            val reminderIntent = Intent().apply {
                putExtra(REMINDERSITEM, remindersItem[position].documentID)
            }

            views.setOnClickFillInIntent(R.id.remindersCheckedButton, positionIntent)

            views.setOnClickFillInIntent(R.id.remindersTextView, reminderIntent)


            if (position == selectedPosition) {
                views.setViewVisibility(R.id.remindersCheckedStauts, View.VISIBLE)
                views.setTextColor(R.id.remindersTextView, MyApplication.instance.getColor(R.color.delete_red))
                
                updateItem(remindersItem[position].documentID)


            } else {
                views.setViewVisibility(R.id.remindersCheckedStauts, View.GONE)
                views.setTextColor(R.id.remindersTextView, MyApplication.instance.getColor(R.color.black))

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

        private // update isChecked to true when user click the button
        fun updateItem(documentID: String) {

            db.collection(DATA)
                .document(UserManager.id!!)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        // add countdowns
                        db.collection(DATA)
                            .document(UserManager.id!!)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(DOCUMENTID, documentID)
                            .get()
                            .addOnSuccessListener { documents ->

                                for (reminders in documents) {

                                    // add countdowns
                                    db.collection(DATA)
                                        .document(UserManager.id!!)
                                        .collection(CALENDAR)
                                        .document(calendar.id)
                                        .collection(REMINDERS)
                                        .document(documentID)
                                        .update(ISCHECKED, true)
                                        .addOnSuccessListener {

                                            // update success
                                            refreshData()

                                        }
                                }
                            }
                    }
                }
        }

        private fun refreshData() {
            db.collection(DATA)
                .document(UserManager.id!!)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { totalDocuments ->

                    remindersItem.clear()

                    for ((index, calendar) in totalDocuments.withIndex()) {

                        //get reminders ( only ischecked is false )
                        db.collection(DATA)
                            .document(UserManager.id!!)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(ISCHECKED, false)
                            .get()
                            .addOnSuccessListener { documents ->

                                for (reminder in documents) {

                                    val setDate = (reminder.data[SETDATE] as Timestamp)
                                    val remindDate = (reminder.data[REMINDDATE] as Timestamp)

                                    remindAdd = Reminders(
                                        simpleDateFormat.format(setDate.seconds * 1000),
                                        reminder.data[TITLE].toString(),
                                        reminder.data[SETREMINDATE].toString().toBoolean(),
                                        simpleDateFormat.format(remindDate.seconds * 1000),
                                        reminder.data[REMINDDATE] as Timestamp,
                                        reminder.data[ISCHECKED].toString().toBoolean(),
                                        reminder.data[NOTE].toString(),
                                        reminder.data[FREQUENCY].toString(),
                                        reminder.data[DOCUMENTID].toString()
                                    )
                                    remindersItem.add(remindAdd)
                                }

                                if (index == totalDocuments.size() -1 ) {
                                    selectedPosition = -1
                                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.remindersWidgetStackView)

                                }
                            }
                    }

                }
        }
    }


}
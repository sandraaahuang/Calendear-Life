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
import com.sandra.calendearlife.getRemindersItemFromFirebase
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.POSITION
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.REFRESHID
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.REMINDERSITEM
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.Logger
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.widget.RemindersWidget.Companion.selectedPosition


class RemindersWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetItemFactory(MyApplication.instance, intent!!)
    }

    class WidgetItemFactory(private val context: Context, intent: Intent) : RemoteViewsFactory {
        var db = FirebaseFirestore.getInstance()

        private val remindersItem = ArrayList<Reminders>()

        private var appWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID
            , AppWidgetManager.INVALID_APPWIDGET_ID)

        override fun onCreate() {

            val appWidgetManager = AppWidgetManager.getInstance(context)

            UserManager.id?.let {
                db.collection(DATA)
                    .document(it)
                    .collection(CALENDAR)
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            //get reminders ( only is_checked is false )
                            db.collection(DATA)
                                .document(it)
                                .collection(CALENDAR)
                                .document(calendar.id)
                                .collection(REMINDERS)
                                .whereEqualTo(IS_CHECKED, false)
                                .get()
                                .addOnSuccessListener { remindersDocuments ->

                                    for (reminder in remindersDocuments) {

                                        getRemindersItemFromFirebase(reminder, remindersItem)
                                    }

                                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.remindersWidgetStackView)
                                }
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

            if (remindersItem[position].hasRemindDate){
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
                views.setTextColor(R.id.remindersTextView, MyApplication.instance.getColor(R.color.primary_gray))
                
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

            UserManager.id?.let {
                db.collection(DATA)
                    .document(it)
                    .collection(CALENDAR)
                    .get()
                    .addOnSuccessListener { documents ->

                        for (calendar in documents) {

                            // add countdowns
                            db.collection(DATA)
                                .document(it)
                                .collection(CALENDAR)
                                .document(calendar.id)
                                .collection(REMINDERS)
                                .whereEqualTo(DOCUMENT_ID, documentID)
                                .get()
                                .addOnSuccessListener { remindersDocuments ->

                                    for (reminders in remindersDocuments) {

                                        // add countdowns
                                        db.collection(DATA)
                                            .document(it)
                                            .collection(CALENDAR)
                                            .document(calendar.id)
                                            .collection(REMINDERS)
                                            .document(documentID)
                                            .update(IS_CHECKED, true)
                                            .addOnSuccessListener {

                                                // update success
                                                refreshData()

                                            }
                                    }
                                }
                        }
                    }
            }
        }

        private fun refreshData() {
            UserManager.id?.let {
                db.collection(DATA)
                    .document(it)
                    .collection(CALENDAR)
                    .get()
                    .addOnSuccessListener { totalDocuments ->

                        remindersItem.clear()

                        for ((index, calendar) in totalDocuments.withIndex()) {

                            //get reminders ( only ischecked is false )
                            db.collection(DATA)
                                .document(it)
                                .collection(CALENDAR)
                                .document(calendar.id)
                                .collection(REMINDERS)
                                .whereEqualTo(IS_CHECKED, false)
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (reminder in documents) {

                                        getRemindersItemFromFirebase(reminder, remindersItem)
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


}
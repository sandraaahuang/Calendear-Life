package com.sandra.calendearlife.calendar.event

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager
import android.provider.CalendarContract.Calendars
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.sandra.calendearlife.MyApplication
import java.util.*


class CalendarEventViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    fun writeItem(item: Any, countdown: Any, reminder: Any) {

        // get all data from user at first
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .add(item)
            .addOnSuccessListener { CdocumentReference ->
                Log.d(
                    "AddCountdownsIntoDB",
                    "DocumentSnapshot added with ID: " + CdocumentReference.id
                )

                // update calendar document id and color ( pure calendar first)
                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .document(CdocumentReference.id)
                    .update("documentID", CdocumentReference.id, "color", "af8eb5")
                    .addOnSuccessListener {
                        // add reminders
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .whereEqualTo("hasReminders", true)
                            .whereEqualTo("documentID", CdocumentReference.id)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    for (document in task.result!!) {
                                        Log.d("All calendar", document.id + " => " + document.data)
                                        // add reminders
                                        db.collection("data")
                                            .document(UserManager.id!!)
                                            .collection("calendar")
                                            .document(CdocumentReference.id)
                                            .collection("reminders")
                                            .add(reminder)
                                            .addOnSuccessListener { documentReference ->
                                                Log.d(
                                                    "AddCountdownsIntoDB",
                                                    "DocumentSnapshot added with ID: " + documentReference.id
                                                )
                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .document(document.id)
                                                    .collection("reminders")
                                                    .document(documentReference.id)
                                                    .update("documentID", documentReference.id)

                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .document(document.id)
                                                    .update("color", "81b9bf")
                                            }
                                    }
                                }
                            }
                        // add countdown
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .whereEqualTo("hasCountdown", true)
                            .whereEqualTo("documentID", CdocumentReference.id)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    for (document in task.result!!) {
                                        Log.d("All calendar", document.id + " => " + document.data)
                                        // add reminders
                                        db.collection("data")
                                            .document(UserManager.id!!)
                                            .collection("calendar")
                                            .document(CdocumentReference.id)
                                            .collection("countdowns")
                                            .add(countdown)
                                            .addOnSuccessListener { documentReference ->
                                                Log.d(
                                                    "AddCountdownsIntoDB",
                                                    "DocumentSnapshot added with ID: " + documentReference.id
                                                )
                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .document(document.id)
                                                    .collection("countdowns")
                                                    .document(documentReference.id)
                                                    .update("documentID", documentReference.id)


                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .document(document.id)
                                                    .update("color", "cb9b8c")


                                                // item that have both reminders and countdown

                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .whereEqualTo("hasCountdown", true)
                                                    .whereEqualTo("hasReminders", true)
                                                    .whereEqualTo("documentID", CdocumentReference.id)
                                                    .get()
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            for (document in task.result!!) {
                                                                Log.d("All calendar", document.id + " => " + document.data)
                                                                //update color

                                                                db.collection("data")
                                                                    .document(UserManager.id!!)
                                                                    .collection("calendar")
                                                                    .document(document.id)
                                                                    .update("color", "a69b97")


                                                            }
                                                        }
                                                    }

                                            }
                                    }
                                }
                            }


                    }
            }
    }

    // insert new item into google calendar
    // auto generate event ID
    fun insert_event() {

        // calendar id
        val targetCalendarId = "3"
        val calendarId = java.lang.Long.parseLong(targetCalendarId)

        // 取得現在的時間作為活動開始時間  begin date
        val currentTimeMillis = System.currentTimeMillis()

        // 設定活動結束時間為15分鐘後 end date
        val endTimeMillis = currentTimeMillis + 900000

        // title
        val targetTitle :String = ""
        // 新增活動
        val cr = MyApplication.instance.contentResolver
        val values = ContentValues()
        values.put(CalendarContract.Events.DTSTART, currentTimeMillis)
        values.put(CalendarContract.Events.DTEND, endTimeMillis)
        values.put(CalendarContract.Events.TITLE, targetTitle)
        values.put(CalendarContract.Events.DESCRIPTION, "Description")
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().displayName)
        // 因為targetSDK=25，所以要在Apps運行時檢查權限
        val permissionCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance,
            Manifest.permission.WRITE_CALENDAR
        )
        // 如果使用者給了權限便開始新增日歷
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = cr?.insert(CalendarContract.Events.CONTENT_URI, values)
            // 返回新建活動的ID
            if (uri != null) {
                val eventID = java.lang.Long.parseLong(uri.lastPathSegment!!)
                // give event id
//                val targetEventId = binding.eventId
//                targetEventId.setText(String.format("%s", eventID))
            }
        }
    }
}
package com.sandra.calendearlife.calendar.event

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.MyApplication
import java.text.SimpleDateFormat
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
                    .update("documentID", CdocumentReference.id, "color", "8C6B8B")
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
                                                    .update("color", "542437")
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
                                                                Log.d(
                                                                    "All calendar",
                                                                    document.id + " => " + document.data
                                                                )
                                                                //update color

                                                                db.collection("data")
                                                                    .document(UserManager.id!!)
                                                                    .collection("calendar")
                                                                    .document(document.id)
                                                                    .update("color", "A6292F")


                                                            }
                                                        }
                                                    }

                                            }
                                    }
                                }
                            }

                        // update google item
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .whereEqualTo("fromGoogle", true)
                            .whereEqualTo("documentID", CdocumentReference.id)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    for (document in task.result!!) {
                                        Log.d("All calendar", document.id + " => " + document.data)

                                        db.collection("data")
                                            .document(UserManager.id!!)
                                            .collection("calendar")
                                            .document(document.id)
                                            .update("color", "245E2C")

                                    }
                                }
                            }
                    }
            }
    }

    fun insertIntoGoogle(beginDate: Timestamp, endDate: Timestamp, title: String, note: String) {
        val EVENT_PROJECTION = arrayOf(
            Calendars._ID, // 0 calendar id
            Calendars.ACCOUNT_NAME, // 1 account name
            Calendars.CALENDAR_DISPLAY_NAME, // 2 display name
            Calendars.OWNER_ACCOUNT, // 3 owner account
            Calendars.CALENDAR_ACCESS_LEVEL
        )// 4 access level

        val PROJECTION_ID_INDEX = 0
        val PROJECTION_ACCOUNT_NAME_INDEX = 1
        val PROJECTION_DISPLAY_NAME_INDEX = 2
        val PROJECTION_OWNER_ACCOUNT_INDEX = 3
        val PROJECTION_CALENDAR_ACCESS_LEVEL = 4

        // Get user email
        val targetAccount = UserManager.userEmail!!
        // search calendar
        val cur: Cursor?
        val cr = MyApplication.instance.contentResolver
        val uri = Calendars.CONTENT_URI
        // find
        val selection = ("((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))")
        val selectionArgs =
            arrayOf(targetAccount, "com.google", UserManager.userEmail)

        //check permission
        val permissionCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance,
            Manifest.permission.READ_CALENDAR
        )
        // create list to store result
        val accountNameList = ArrayList<String>()
        val calendarIdList = ArrayList<String>()

        // give permission to read
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            cur = cr?.query(uri, EVENT_PROJECTION, selection, selectionArgs, null)
            if (cur != null) {
                while (cur.moveToNext()) {
                    val calendarId: String = cur.getString(PROJECTION_ID_INDEX)
                    val accountName: String = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                    val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                    val ownerAccount: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                    val accessLevel = cur.getInt(PROJECTION_CALENDAR_ACCESS_LEVEL)

                    Log.i("query_calendar", String.format("calendarId=%s", calendarId))
                    Log.i("query_calendar", String.format("accountName=%s", accountName))
                    Log.i("query_calendar", String.format("displayName=%s", displayName))
                    Log.i("query_calendar", String.format("ownerAccount=%s", ownerAccount))
                    Log.i("query_calendar", String.format("accessLevel=%s", accessLevel))
                    // store calendar data
                    accountNameList.add(displayName)
                    calendarIdList.add(calendarId)

                    Log.d("sandraaa", "accountNameList = $accountNameList,calendarIdList = $calendarIdList ")

                    val targetCalendar = calendarId

                    // calendar id
                    val targetCalendarId = targetCalendar

                    // add event
                    val cr = MyApplication.instance.contentResolver
                    val values = ContentValues()
                    values.put(CalendarContract.Events.DTSTART, beginDate.seconds * 1000)
                    values.put(
                        CalendarContract.Events.DTEND, endDate.seconds * 1000
                    )
                    values.put(CalendarContract.Events.TITLE, title)
                    values.put(CalendarContract.Events.DESCRIPTION, note)
                    values.put(CalendarContract.Events.CALENDAR_ID, targetCalendarId)
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
                            Log.d("sandraaa", "eventID = $eventID")
                        }
                    }

                }

                cur.close()
            }
        } else {
            val toast = Toast.makeText(MyApplication.instance, "沒有所需的權限", Toast.LENGTH_LONG)
            toast.show()
        }
    }

}
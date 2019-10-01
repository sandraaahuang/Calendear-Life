package com.sandra.calendearlife.calendar.event

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.util.UserManager
import java.util.*


class CalendarEventViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private var _isUpdateCompleted = MutableLiveData<Boolean>()

    val isUpdateCompleted: LiveData<Boolean>
        get() = _isUpdateCompleted

    private var _isClicked = MutableLiveData<Boolean>()

    val isClicked: LiveData<Boolean>
        get() = _isClicked

    fun writeItem(item: Any, countdown: Any, reminder: Any) {
        _isClicked.value = true
        // get all data from user at first
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .add(item)
            .addOnSuccessListener { CdocumentReference ->
                Log.d(
                    "AddNewCalendar",
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

                                        // add reminders
                                        db.collection("data")
                                            .document(UserManager.id!!)
                                            .collection("calendar")
                                            .document(CdocumentReference.id)
                                            .collection("reminders")
                                            .add(reminder)
                                            .addOnSuccessListener { documentReference ->
                                                Log.d(
                                                    "AddNewReminders",
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

                                        // add countdown
                                        db.collection("data")
                                            .document(UserManager.id!!)
                                            .collection("calendar")
                                            .document(CdocumentReference.id)
                                            .collection("countdowns")
                                            .add(countdown)
                                            .addOnSuccessListener { documentReference ->
                                                Log.d(
                                                    "AddNewCountdowns",
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
            .addOnCompleteListener {
                _isUpdateCompleted.value = true
            }
    }

    fun writeGoogle(

        gBeginDate: Timestamp, gEndDate: Timestamp, gNote: String, gTitle: String,
        item: Any, countdown: Any, reminders: Any
    ) {
        _isClicked.value = true
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
                    values.put(CalendarContract.Events.DTSTART, gBeginDate.seconds * 1000)
                    values.put(
                        CalendarContract.Events.DTEND, gEndDate.seconds * 1000
                    )
                    values.put(CalendarContract.Events.TITLE, gTitle)
                    values.put(CalendarContract.Events.DESCRIPTION, gNote)
                    values.put(CalendarContract.Events.CALENDAR_ID, targetCalendarId)
                    values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().displayName)

                    val permissionCheck = ContextCompat.checkSelfPermission(
                        MyApplication.instance,
                        Manifest.permission.WRITE_CALENDAR
                    )

                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        val uri = cr?.insert(CalendarContract.Events.CONTENT_URI, values)
                        // get Event ID
                        if (uri != null) {
                            val eventID = java.lang.Long.parseLong(uri.lastPathSegment!!)
                            Log.d("sandraaa", "eventID = $eventID")

                            // get all data from user at first
                            db.collection("data")
                                .document(UserManager.id!!)
                                .collection("calendar")
                                .document(eventID.toString())
                                .set(item)
                                .addOnSuccessListener { CdocumentReference ->


                                    // update calendar document id and color ( pure calendar first)
                                    db.collection("data")
                                        .document(UserManager.id!!)
                                        .collection("calendar")
                                        .document(eventID.toString())
                                        .update("documentID", eventID.toString(), "color", "8C6B8B")
                                        .addOnSuccessListener {
                                            // add reminders
                                            db.collection("data")
                                                .document(UserManager.id!!)
                                                .collection("calendar")
                                                .whereEqualTo("hasReminders", true)
                                                .whereEqualTo("documentID", eventID.toString())
                                                .get()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        for (document in task.result!!) {

                                                            // add reminders
                                                            db.collection("data")
                                                                .document(UserManager.id!!)
                                                                .collection("calendar")
                                                                .document(eventID.toString())
                                                                .collection("reminders")
                                                                .add(reminders)
                                                                .addOnSuccessListener { documentReference ->
                                                                    Log.d(
                                                                        "AddNewReminders",
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
                                                .whereEqualTo("documentID", eventID.toString())
                                                .get()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        for (document in task.result!!) {

                                                            // add countdown
                                                            db.collection("data")
                                                                .document(UserManager.id!!)
                                                                .collection("calendar")
                                                                .document(eventID.toString())
                                                                .collection("countdowns")
                                                                .add(countdown)
                                                                .addOnSuccessListener { documentReference ->
                                                                    Log.d(
                                                                        "AddNewCountdowns",
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
                                                                        .whereEqualTo("documentID", eventID.toString())
                                                                        .get()
                                                                        .addOnCompleteListener { task ->
                                                                            if (task.isSuccessful) {
                                                                                for (document in task.result!!) {

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
                                                .whereEqualTo("documentID", eventID.toString())
                                                .get()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        for (document in task.result!!) {

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
                                .addOnCompleteListener {
                                    _isUpdateCompleted.value = true
                                }
                        }
                    }

                }

                cur.close()
            }
        }
    }
}

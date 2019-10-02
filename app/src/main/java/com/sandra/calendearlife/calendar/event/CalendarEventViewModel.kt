package com.sandra.calendearlife.calendar.event

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_ALL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CONJUNCTION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROMGOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASCOUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASREMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.MAILFORMAT
import com.sandra.calendearlife.constant.FirebaseKey.Companion.PARENTHESES
import com.sandra.calendearlife.constant.FirebaseKey.Companion.QUESTIONMARK
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.util.Logger
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

    private var _showDateWeekPicker = MutableLiveData<TextView>()
    val showDateWeekPicker: LiveData<TextView>
        get() = _showDateWeekPicker

    private var _showDatePicker = MutableLiveData<TextView>()
    val showDatePicker: LiveData<TextView>
        get() = _showDatePicker

    private var _showTimePicker = MutableLiveData<TextView>()
    val showTimePicker: LiveData<TextView>
        get() = _showTimePicker

    fun writeItem(item: Any, countdown: Any, reminder: Any) {
        _isClicked.value = true
        // get all data from user at first
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .add(item)
            .addOnSuccessListener { calendarDocumentReference ->

                // update calendar document id and color ( pure calendar first)
                db.collection(DATA)
                    .document(UserManager.id!!)
                    .collection(CALENDAR)
                    .document(calendarDocumentReference.id)
                    .update(DOCUMENTID, calendarDocumentReference.id, COLOR, COLOR_CAL)
                    .addOnSuccessListener {
                        // add reminders
                        db.collection(DATA)
                            .document(UserManager.id!!)
                            .collection(CALENDAR)
                            .whereEqualTo(HASREMINDERS, true)
                            .whereEqualTo(DOCUMENTID, calendarDocumentReference.id)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    for (document in task.result!!) {

                                        // add reminders
                                        db.collection(DATA)
                                            .document(UserManager.id!!)
                                            .collection(CALENDAR)
                                            .document(calendarDocumentReference.id)
                                            .collection(REMINDERS)
                                            .add(reminder)
                                            .addOnSuccessListener { documentReference ->

                                                db.collection(DATA)
                                                    .document(UserManager.id!!)
                                                    .collection(CALENDAR)
                                                    .document(document.id)
                                                    .collection(REMINDERS)
                                                    .document(documentReference.id)
                                                    .update(DOCUMENTID, documentReference.id)

                                                db.collection(DATA)
                                                    .document(UserManager.id!!)
                                                    .collection(CALENDAR)
                                                    .document(document.id)
                                                    .update(COLOR, COLOR_REMIND_CAL)
                                            }
                                    }
                                }
                            }
                        // add countdown
                        db.collection(DATA)
                            .document(UserManager.id!!)
                            .collection(CALENDAR)
                            .whereEqualTo(HASREMINDERS, true)
                            .whereEqualTo(DOCUMENTID, calendarDocumentReference.id)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    for (document in task.result!!) {

                                        // add countdown
                                        db.collection(DATA)
                                            .document(UserManager.id!!)
                                            .collection(CALENDAR)
                                            .document(calendarDocumentReference.id)
                                            .collection(COUNTDOWN)
                                            .add(countdown)
                                            .addOnSuccessListener { documentReference ->

                                                db.collection(DATA)
                                                    .document(UserManager.id!!)
                                                    .collection(CALENDAR)
                                                    .document(document.id)
                                                    .collection(COUNTDOWN)
                                                    .document(documentReference.id)
                                                    .update(DOCUMENTID, documentReference.id)


                                                db.collection(DATA)
                                                    .document(UserManager.id!!)
                                                    .collection(CALENDAR)
                                                    .document(document.id)
                                                    .update(COLOR, COLOR_COUNTDOWN_CAL)


                                                // item that have both reminders and countdown
                                                db.collection(DATA)
                                                    .document(UserManager.id!!)
                                                    .collection(CALENDAR)
                                                    .whereEqualTo(HASCOUNTDOWN, true)
                                                    .whereEqualTo(HASREMINDERS, true)
                                                    .whereEqualTo(DOCUMENTID, calendarDocumentReference.id)
                                                    .get()
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            for (document in task.result!!) {

                                                                //update color

                                                                db.collection(DATA)
                                                                    .document(UserManager.id!!)
                                                                    .collection(CALENDAR)
                                                                    .document(document.id)
                                                                    .update(COLOR, COLOR_ALL)
                                                            }
                                                        }
                                                    }
                                            }
                                    }
                                }
                            }

                        // update google item
                        db.collection(DATA)
                            .document(UserManager.id!!)
                            .collection(CALENDAR)
                            .whereEqualTo(FROMGOOGLE, true)
                            .whereEqualTo(DOCUMENTID, calendarDocumentReference.id)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    for (document in task.result!!) {

                                        db.collection(DATA)
                                            .document(UserManager.id!!)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .update(COLOR, COLOR_GOOGLE)

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
        val eventProjection = arrayOf(
            Calendars._ID, // 0 calendar id
            Calendars.ACCOUNT_NAME, // 1 account name
            Calendars.CALENDAR_DISPLAY_NAME, // 2 display name
            Calendars.OWNER_ACCOUNT, // 3 owner account
            Calendars.CALENDAR_ACCESS_LEVEL
        )// 4 access level

        val projectionIdIndex = 0
        val projectionAccountNameIndex = 1
        val projectionDisplayNameIndex = 2
        val projectionOwnerAccountIndex = 3
        val projectionCalendarAccessLevel = 4

        // Get user email
        val targetAccount = UserManager.userEmail!!
        // search calendar
        val cur: Cursor?
        val cr = MyApplication.instance.contentResolver
        val uri = Calendars.CONTENT_URI
        // find
        val selection = (PARENTHESES + Calendars.ACCOUNT_NAME + CONJUNCTION
                + Calendars.ACCOUNT_TYPE + CONJUNCTION
                + Calendars.OWNER_ACCOUNT + QUESTIONMARK)
        val selectionArgs =
            arrayOf(targetAccount, MAILFORMAT, UserManager.userEmail)

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
            cur = cr?.query(uri, eventProjection, selection, selectionArgs, null)
            if (cur != null) {
                while (cur.moveToNext()) {
                    val calendarId: String = cur.getString(projectionIdIndex)
                    val accountName: String = cur.getString(projectionAccountNameIndex)
                    val displayName: String = cur.getString(projectionDisplayNameIndex)
                    val ownerAccount: String = cur.getString(projectionOwnerAccountIndex)
                    val accessLevel = cur.getInt(projectionCalendarAccessLevel)

                    Logger.i(String.format("calendarId=%s", calendarId))
                    Logger.i(String.format("accountName=%s", accountName))
                    Logger.i(String.format("displayName=%s", displayName))
                    Logger.i(String.format("ownerAccount=%s", ownerAccount))
                    Logger.i(String.format("accessLevel=%s", accessLevel))

                    // store calendar data
                    accountNameList.add(displayName)
                    calendarIdList.add(calendarId)

                    Logger.d("accountNameList = $accountNameList, calendarIdList = $calendarIdList")

                    // add event
                    val cr = MyApplication.instance.contentResolver
                    val values = ContentValues()
                    values.put(CalendarContract.Events.DTSTART, gBeginDate.seconds * 1000)
                    values.put(
                        CalendarContract.Events.DTEND, gEndDate.seconds * 1000
                    )
                    values.put(CalendarContract.Events.TITLE, gTitle)
                    values.put(CalendarContract.Events.DESCRIPTION, gNote)
                    values.put(CalendarContract.Events.CALENDAR_ID, calendarId)
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

                            // get all data from user at first
                            db.collection(DATA)
                                .document(UserManager.id!!)
                                .collection(CALENDAR)
                                .document(eventID.toString())
                                .set(item)
                                .addOnSuccessListener {

                                    // update calendar document id and color ( pure calendar first)
                                    db.collection(DATA)
                                        .document(UserManager.id!!)
                                        .collection(CALENDAR)
                                        .document(eventID.toString())
                                        .update(DOCUMENTID, eventID.toString(), COLOR, COLOR_CAL)
                                        .addOnSuccessListener {
                                            // add reminders
                                            db.collection(DATA)
                                                .document(UserManager.id!!)
                                                .collection(CALENDAR)
                                                .whereEqualTo(HASREMINDERS, true)
                                                .whereEqualTo(DOCUMENTID, eventID.toString())
                                                .get()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        for (document in task.result!!) {

                                                            // add reminders
                                                            db.collection(DATA)
                                                                .document(UserManager.id!!)
                                                                .collection(CALENDAR)
                                                                .document(eventID.toString())
                                                                .collection(REMINDERS)
                                                                .add(reminders)
                                                                .addOnSuccessListener { documentReference ->

                                                                    db.collection(DATA)
                                                                        .document(UserManager.id!!)
                                                                        .collection(CALENDAR)
                                                                        .document(document.id)
                                                                        .collection(REMINDERS)
                                                                        .document(documentReference.id)
                                                                        .update(DOCUMENTID, documentReference.id)

                                                                    db.collection(DATA)
                                                                        .document(UserManager.id!!)
                                                                        .collection(CALENDAR)
                                                                        .document(document.id)
                                                                        .update(COLOR, COLOR_REMIND_CAL)
                                                                }
                                                        }
                                                    }
                                                }
                                            // add countdown
                                            db.collection(DATA)
                                                .document(UserManager.id!!)
                                                .collection(CALENDAR)
                                                .whereEqualTo(HASCOUNTDOWN, true)
                                                .whereEqualTo(DOCUMENTID, eventID.toString())
                                                .get()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        for (document in task.result!!) {

                                                            // add countdown
                                                            db.collection(DATA)
                                                                .document(UserManager.id!!)
                                                                .collection(CALENDAR)
                                                                .document(eventID.toString())
                                                                .collection(COUNTDOWN)
                                                                .add(countdown)
                                                                .addOnSuccessListener { documentReference ->

                                                                    db.collection(DATA)
                                                                        .document(UserManager.id!!)
                                                                        .collection(CALENDAR)
                                                                        .document(document.id)
                                                                        .collection(COUNTDOWN)
                                                                        .document(documentReference.id)
                                                                        .update(DOCUMENTID, documentReference.id)


                                                                    db.collection(DATA)
                                                                        .document(UserManager.id!!)
                                                                        .collection(CALENDAR)
                                                                        .document(document.id)
                                                                        .update(COLOR, COLOR_COUNTDOWN_CAL)


                                                                    // item that have both reminders and countdown
                                                                    db.collection(DATA)
                                                                        .document(UserManager.id!!)
                                                                        .collection(CALENDAR)
                                                                        .whereEqualTo(HASCOUNTDOWN, true)
                                                                        .whereEqualTo(HASREMINDERS, true)
                                                                        .whereEqualTo(DOCUMENTID, eventID.toString())
                                                                        .get()
                                                                        .addOnCompleteListener { task ->
                                                                            if (task.isSuccessful) {
                                                                                for (document in task.result!!) {

                                                                                    //update color

                                                                                    db.collection(DATA)
                                                                                        .document(UserManager.id!!)
                                                                                        .collection(CALENDAR)
                                                                                        .document(document.id)
                                                                                        .update(COLOR, COLOR_ALL)
                                                                                }
                                                                            }
                                                                        }
                                                                }
                                                        }
                                                    }
                                                }

                                            // update google item
                                            db.collection(DATA)
                                                .document(UserManager.id!!)
                                                .collection(CALENDAR)
                                                .whereEqualTo(FROMGOOGLE, true)
                                                .whereEqualTo(DOCUMENTID, eventID.toString())
                                                .get()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        for (document in task.result!!) {

                                                            db.collection(DATA)
                                                                .document(UserManager.id!!)
                                                                .collection(CALENDAR)
                                                                .document(document.id)
                                                                .update(COLOR, COLOR_GOOGLE)
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

    fun showDateWeekPicker(clickText: TextView) {
        _showDateWeekPicker.value = clickText
    }

    fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }

    fun showTimePicker(clickText: TextView) {
        _showTimePicker.value = clickText
    }
}

package com.sandra.calendearlife.calendar.event

import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_ALL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND_CAL
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROM_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.contentResolver
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.contentUri
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.eventProjection
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.permissionReadCheck
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_ACCOUNT_NAME_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_CALENDAR_ACCESS_LEVEL
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_DISPLAY_NAME_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_ID_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.PROJECTION_OWNER_ACCOUNT_INDEX
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.SELECTION
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.selectionArgs
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
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .add(item)
                .addOnSuccessListener { calendarDocumentReference ->

                    // update calendar document id and color ( pure calendar first)
                    db.collection(DATA)
                        .document(userManagerId)
                        .collection(CALENDAR)
                        .document(calendarDocumentReference.id)
                        .update(DOCUMENT_ID, calendarDocumentReference.id, COLOR, COLOR_CAL)
                        .addOnSuccessListener {

                            writeHasRemindersItem(calendarDocumentReference, reminder)
                            writeHasCountdownItem(calendarDocumentReference, countdown)
                            writeGoogleItem(calendarDocumentReference)

                        }
                }
                .addOnCompleteListener {
                    _isUpdateCompleted.value = true
                }
        }
    }

    private fun writeHasRemindersItem(documentReference: DocumentReference, reminder: Any) {
        // add reminders
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(HAS_REMINDERS, true)
                .whereEqualTo(DOCUMENT_ID, documentReference.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            for (document in it) {

                                // add reminders
                                db.collection(DATA)
                                    .document(userManagerId)
                                    .collection(CALENDAR)
                                    .document(documentReference.id)
                                    .collection(REMINDERS)
                                    .add(reminder)
                                    .addOnSuccessListener { documentReference ->

                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .collection(REMINDERS)
                                            .document(documentReference.id)
                                            .update(DOCUMENT_ID, documentReference.id)

                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .update(COLOR, COLOR_REMIND_CAL)
                                    }
                            }
                        }

                    }
                }
        }
    }

    private fun writeHasCountdownItem(documentReference: DocumentReference, countdown: Any) {
        // add countdown
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(HAS_COUNTDOWN, true)
                .whereEqualTo(DOCUMENT_ID, documentReference.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            for (document in it) {

                                // add countdown
                                db.collection(DATA)
                                    .document(userManagerId)
                                    .collection(CALENDAR)
                                    .document(documentReference.id)
                                    .collection(COUNTDOWN)
                                    .add(countdown)
                                    .addOnSuccessListener { documentReference ->

                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .collection(COUNTDOWN)
                                            .document(documentReference.id)
                                            .update(DOCUMENT_ID, documentReference.id)


                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .update(COLOR, COLOR_COUNTDOWN_CAL)

                                        // all have
                                        writeHasRemindersAndCountdown(documentReference)
                                    }
                            }
                        }
                    }
                }
        }
    }

    private fun writeHasRemindersAndCountdown(documentReference: DocumentReference) {
        // item that have both reminders and countdown
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(HAS_COUNTDOWN, true)
                .whereEqualTo(HAS_REMINDERS, true)
                .whereEqualTo(DOCUMENT_ID, documentReference.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            for (document in it) {

                                //update color
                                db.collection(DATA)
                                    .document(userManagerId)
                                    .collection(CALENDAR)
                                    .document(document.id)
                                    .update(COLOR, COLOR_ALL)
                            }
                        }

                    }
                }
        }
    }

    private fun writeGoogleItem(documentReference: DocumentReference) {
        // update google item
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(FROM_GOOGLE, true)
                .whereEqualTo(DOCUMENT_ID, documentReference.id)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            for (document in it) {

                                db.collection(DATA)
                                    .document(userManagerId)
                                    .collection(CALENDAR)
                                    .document(document.id)
                                    .update(COLOR, COLOR_GOOGLE)

                            }
                        }

                    }
                }
        }
    }

    fun writeGoogle(
        googleBeginDate: Timestamp, googleEndDate: Timestamp,
        googleNote: String, googleTitle: String,
        item: Any, countdown: Any, reminders: Any
    ) {
        _isClicked.value = true

        // search calendar
        val cur: Cursor?

        // create list to store result
        val accountNameList = ArrayList<String>()
        val calendarIdList = ArrayList<String>()

        // give permission to read
        if (permissionReadCheck == PackageManager.PERMISSION_GRANTED) {
            cur = contentResolver.query(contentUri, eventProjection, SELECTION, selectionArgs, null)
            cur?.let {
                while (cur.moveToNext()) {
                    val calendarId = cur.getString(PROJECTION_ID_INDEX)
                    val accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX)
                    val displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
                    val ownerAccount = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
                    val accessLevel = cur.getInt(PROJECTION_CALENDAR_ACCESS_LEVEL)

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
                    ContentValues().put(CalendarContract.Events.DTSTART, googleBeginDate.seconds * 1000)
                    ContentValues().put(
                        CalendarContract.Events.DTEND, googleEndDate.seconds * 1000
                    )
                    ContentValues().put(CalendarContract.Events.TITLE, googleTitle)
                    ContentValues().put(CalendarContract.Events.DESCRIPTION, googleNote)
                    ContentValues().put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    ContentValues().put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().displayName)

                    if (permissionReadCheck == PackageManager.PERMISSION_GRANTED) {
                        contentResolver.insert(
                            CalendarContract.Events.CONTENT_URI, ContentValues())?.let {

                            it.lastPathSegment?.let { lastPathSegment ->
                                val eventID = java.lang.Long.parseLong(lastPathSegment).toString()

                                // get all data from user at first
                                UserManager.id?.let { userManagerId ->
                                    db.collection(DATA)
                                        .document(userManagerId)
                                        .collection(CALENDAR)
                                        .document(eventID)
                                        .set(item)
                                        .addOnSuccessListener {

                                            // update calendar document id and color ( pure calendar first)
                                            UserManager.id?.let { userManagerId ->
                                                db.collection(DATA)
                                                    .document(userManagerId)
                                                    .collection(CALENDAR)
                                                    .document(eventID)
                                                    .update(DOCUMENT_ID, eventID, COLOR, COLOR_CAL)
                                                    .addOnSuccessListener {

                                                        writeGoogleHasReminders(eventID, reminders)
                                                        writeGoogleHasCountdown(eventID, countdown)
                                                        updateGoogleItem(eventID)

                                                    }
                                            }

                                        }
                                        .addOnCompleteListener {
                                            _isUpdateCompleted.value = true
                                        }
                                }
                            }

                        }
                    }

                }

                cur.close()
            }
        } else {
            Toast.makeText(
                MyApplication.instance, MyApplication.instance.getString(R.string.open_permission),
                Toast.LENGTH_LONG).show()
        }
    }

    private fun writeGoogleHasReminders(eventID: String, reminders: Any) {
        // add reminders
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(HAS_REMINDERS, true)
                .whereEqualTo(DOCUMENT_ID, eventID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            for (document in it) {

                                // add reminders
                                db.collection(DATA)
                                    .document(userManagerId)
                                    .collection(CALENDAR)
                                    .document(eventID)
                                    .collection(REMINDERS)
                                    .add(reminders)
                                    .addOnSuccessListener { documentReference ->

                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .collection(REMINDERS)
                                            .document(documentReference.id)
                                            .update(DOCUMENT_ID, documentReference.id)

                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .update(COLOR, COLOR_REMIND_CAL)
                                    }
                            }
                        }
                    }
                }
        }
    }

    private fun writeGoogleHasCountdown(eventID: String, countdown: Any) {
        // add countdown
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(HAS_COUNTDOWN, true)
                .whereEqualTo(DOCUMENT_ID, eventID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            for (document in it) {

                                // add countdown
                                db.collection(DATA)
                                    .document(userManagerId)
                                    .collection(CALENDAR)
                                    .document(eventID)
                                    .collection(COUNTDOWN)
                                    .add(countdown)
                                    .addOnSuccessListener { documentReference ->

                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .collection(COUNTDOWN)
                                            .document(documentReference.id)
                                            .update(DOCUMENT_ID, documentReference.id)


                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .update(COLOR, COLOR_COUNTDOWN_CAL)


                                        // item that have both reminders and countdown
                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .whereEqualTo(HAS_COUNTDOWN, true)
                                            .whereEqualTo(HAS_REMINDERS, true)
                                            .whereEqualTo(DOCUMENT_ID, eventID)
                                            .get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    task.result?.let { taskResult ->
                                                        for (allDocument in taskResult) {

                                                            //update color

                                                            db.collection(DATA)
                                                                .document(userManagerId)
                                                                .collection(CALENDAR)
                                                                .document(allDocument.id)
                                                                .update(COLOR, COLOR_ALL)
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
    }

    private fun updateGoogleItem(eventID: String) {
        // update google item
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(FROM_GOOGLE, true)
                .whereEqualTo(DOCUMENT_ID, eventID)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let {
                            for (document in it) {

                                db.collection(DATA)
                                    .document(userManagerId)
                                    .collection(CALENDAR)
                                    .document(document.id)
                                    .update(COLOR, COLOR_GOOGLE)
                            }
                        }

                    }
                }
        }
    }

    fun onButtonClick(view: View) {
        when (view.id) {
            R.id.beginDate -> showDateWeekPicker(view.findViewById(R.id.beginDate))
            R.id.beginTime -> showTimePicker(view.findViewById(R.id.beginTime))
            R.id.endDate -> showDateWeekPicker(view.findViewById(R.id.endDate))
            R.id.endTime -> showTimePicker(view.findViewById(R.id.endTime))
            R.id.remindersDateInput -> showDatePicker(view.findViewById(R.id.remindersDateInput))
            R.id.remindersTimeInput -> showTimePicker(view.findViewById(R.id.remindersTimeInput))
        }
    }

    private fun showDateWeekPicker(clickText: TextView) {
        _showDateWeekPicker.value = clickText
    }

    private fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }

    private fun showTimePicker(clickText: TextView) {
        _showTimePicker.value = clickText
    }
}

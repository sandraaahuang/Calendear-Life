package com.sandra.calendearlife.calendar.event

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
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
                        .update(DOCUMENTID, calendarDocumentReference.id, COLOR, COLOR_CAL)
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
                .whereEqualTo(HASREMINDERS, true)
                .whereEqualTo(DOCUMENTID, documentReference.id)
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
                                            .update(DOCUMENTID, documentReference.id)

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
                .whereEqualTo(HASCOUNTDOWN, true)
                .whereEqualTo(DOCUMENTID, documentReference.id)
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
                                            .update(DOCUMENTID, documentReference.id)


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
                .whereEqualTo(HASCOUNTDOWN, true)
                .whereEqualTo(HASREMINDERS, true)
                .whereEqualTo(DOCUMENTID, documentReference.id)
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
                .whereEqualTo(FROMGOOGLE, true)
                .whereEqualTo(DOCUMENTID, documentReference.id)
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

        gBeginDate: Timestamp, gEndDate: Timestamp, gNote: String, gTitle: String,
        item: Any, countdown: Any, reminders: Any
    ) {
        _isClicked.value = true
        val eventProjection = arrayOf(
            Calendars._ID,
            Calendars.ACCOUNT_NAME,
            Calendars.CALENDAR_DISPLAY_NAME,
            Calendars.OWNER_ACCOUNT,
            Calendars.CALENDAR_ACCESS_LEVEL
        )// 4 access level

        val projectionIdIndex = 0
        val projectionAccountNameIndex = 1
        val projectionDisplayNameIndex = 2
        val projectionOwnerAccountIndex = 3
        val projectionCalendarAccessLevel = 4

        // Get user email
        val targetAccount = UserManager.userEmail
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
            cur?.let {
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
                    val contentResolver = MyApplication.instance.contentResolver
                    ContentValues().put(CalendarContract.Events.DTSTART, gBeginDate.seconds * 1000)
                    ContentValues().put(
                        CalendarContract.Events.DTEND, gEndDate.seconds * 1000
                    )
                    ContentValues().put(CalendarContract.Events.TITLE, gTitle)
                    ContentValues().put(CalendarContract.Events.DESCRIPTION, gNote)
                    ContentValues().put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    ContentValues().put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().displayName)

                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        val contentUri = contentResolver?.insert(CalendarContract.Events.CONTENT_URI, ContentValues())
                        // get Event ID
                        contentUri?.let {
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
                                                    .update(DOCUMENTID, eventID, COLOR, COLOR_CAL)
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
        }
    }

    private fun writeGoogleHasReminders(eventID: String, reminders: Any) {
        // add reminders
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(HASREMINDERS, true)
                .whereEqualTo(DOCUMENTID, eventID)
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
                                            .update(DOCUMENTID, documentReference.id)

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
                .whereEqualTo(HASCOUNTDOWN, true)
                .whereEqualTo(DOCUMENTID, eventID)
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
                                            .update(DOCUMENTID, documentReference.id)


                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .document(document.id)
                                            .update(COLOR, COLOR_COUNTDOWN_CAL)


                                        // item that have both reminders and countdown
                                        db.collection(DATA)
                                            .document(userManagerId)
                                            .collection(CALENDAR)
                                            .whereEqualTo(HASCOUNTDOWN, true)
                                            .whereEqualTo(HASREMINDERS, true)
                                            .whereEqualTo(DOCUMENTID, eventID)
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
                .whereEqualTo(FROMGOOGLE, true)
                .whereEqualTo(DOCUMENTID, eventID)
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

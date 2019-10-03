package com.sandra.calendearlife.calendar.detail

import android.Manifest.permission.WRITE_CALENDAR
import android.app.Application
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.util.UserManager
import kotlin.collections.HashMap


class CalendarDetailViewModel(calendar: Calendar, app: Application) : AndroidViewModel(app) {
    var db = FirebaseFirestore.getInstance()

    private val _selectedItem = MutableLiveData<Calendar>()
    val selectedItem: LiveData<Calendar>
        get() = _selectedItem

    private var _isUpdateCompleted = MutableLiveData<Boolean>()
    val isUpdateCompleted: LiveData<Boolean>
        get() = _isUpdateCompleted

    private var _isClicked = MutableLiveData<Boolean>()
    val isClicked: LiveData<Boolean>
        get() = _isClicked

    private var _showDatePicker = MutableLiveData<TextView>()
    val showDatePicker: LiveData<TextView>
        get() = _showDatePicker

    private var _showTimePicker = MutableLiveData<TextView>()
    val showTimePicker: LiveData<TextView>
        get() = _showTimePicker

    init {
        _selectedItem.value = calendar
    }

    //update item
    fun updateItem(
        documentID: String,
        calendarItem: HashMap<String, Any>,
        countdown: HashMap<String, Any>,
        updateRemind: HashMap<String, Any>
    ) {

        _isClicked.value = true

        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(DOCUMENTID, documentID)
                .get()
                .addOnSuccessListener { documents ->

                    for ((index, calendar) in documents.withIndex()) {

                        updateCountdownItem(calendar, countdown)
                        updateRemindersItem(calendar, updateRemind)
                        updateCalendarItem(calendar, calendarItem)

                        if (index == documents.size() - 1) {
                            _isUpdateCompleted.value = true
                        }
                    }

                }
        }

    }

    private fun updateCountdownItem(
        queryDocumentSnapshot: QueryDocumentSnapshot,
        countdown: HashMap<String, Any>
    ) {
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .document(queryDocumentSnapshot.id)
                .collection(COUNTDOWN)
                .get()
                .addOnSuccessListener { countdownQuerySnapshot ->

                    for (countdowns in countdownQuerySnapshot) {

                        // update countdowns
                        db.collection(DATA)
                            .document(userManagerId)
                            .collection(CALENDAR)
                            .document(queryDocumentSnapshot.id)
                            .collection(COUNTDOWN)
                            .document(countdowns.id)
                            .update(countdown)
                    }
                }
        }

    }

    private fun updateRemindersItem(
        queryDocumentSnapshot: QueryDocumentSnapshot,
        updateRemind: HashMap<String, Any>
    ) {
        // update reminders
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .document(queryDocumentSnapshot.id)
                .collection(REMINDERS)
                .get()
                .addOnSuccessListener { remindersQuerySnapshot ->

                    for (reminders in remindersQuerySnapshot) {

                        // update reminders
                        db.collection(DATA)
                            .document(userManagerId)
                            .collection(CALENDAR)
                            .document(queryDocumentSnapshot.id)
                            .collection(REMINDERS)
                            .document(reminders.id)
                            .update(updateRemind)
                    }
                }
        }

    }

    private fun updateCalendarItem(
        queryDocumentSnapshot: QueryDocumentSnapshot,
        calendarItem: HashMap<String, Any>
    ) {
        // update calendar
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .document(queryDocumentSnapshot.id)
                .update(calendarItem)
        }

    }

    //delete item
    fun deleteItem(documentID: String) {
        _isClicked.value = true

        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(DOCUMENTID, documentID)
                .get()
                .addOnSuccessListener { calendarQuerySnapshot ->

                    for ((index, calendar) in calendarQuerySnapshot.withIndex()) {

                        deleteCountdownItem(calendar)
                        deleteRemindersItem(calendar)
                        deleteCalendarItem(calendar)

                        if (index == calendarQuerySnapshot.size() - 1) {
                            _isUpdateCompleted.value = true
                        }
                    }
                }
        }

    }

    private fun deleteCountdownItem(queryDocumentSnapshot: QueryDocumentSnapshot) {
        // delete countdowns
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .document(queryDocumentSnapshot.id)
                .collection(COUNTDOWN)
                .get()
                .addOnSuccessListener { documents ->

                    for (countdowns in documents) {

                        db.collection(DATA)
                            .document(userManagerId)
                            .collection(CALENDAR)
                            .document(queryDocumentSnapshot.id)
                            .collection(COUNTDOWN)
                            .document(countdowns.id)
                            .delete()
                    }
                }
        }

    }

    private fun deleteRemindersItem(queryDocumentSnapshot: QueryDocumentSnapshot) {
        // delete reminders
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .document(queryDocumentSnapshot.id)
                .collection(REMINDERS)
                .get()
                .addOnSuccessListener { remindersQuerySnapshot ->

                    for (reminders in remindersQuerySnapshot) {

                        db.collection(DATA)
                            .document(userManagerId)
                            .collection(CALENDAR)
                            .document(queryDocumentSnapshot.id)
                            .collection(REMINDERS)
                            .document(reminders.id)
                            .delete()
                    }
                }
        }

    }

    private fun deleteCalendarItem(queryDocumentSnapshot: QueryDocumentSnapshot) {
        // delete calendar
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .document(queryDocumentSnapshot.id)
                .delete()
        }

    }

    fun updateEvent(eventId: String, title: String, note: String, beginDate: Timestamp, endDate: Timestamp) {
        _isClicked.value = true

        val longEventId = java.lang.Long.parseLong(eventId)

        // update event
        val contentResolver = MyApplication.instance.contentResolver
        val values = ContentValues()
        values.put(CalendarContract.Events.TITLE, title)
        values.put(CalendarContract.Events.DESCRIPTION, note)
        values.put(CalendarContract.Events.DTSTART, beginDate.seconds * 1000)
        values.put(CalendarContract.Events.DTEND, endDate.seconds * 1000)

        val permissionCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance, WRITE_CALENDAR
        )

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, longEventId)
            contentResolver.update(uri, values, null, null)
        }
    }

    fun deleteEvent(eventId: String) {
        _isClicked.value = true

        val longEventId = java.lang.Long.parseLong(eventId)

        val contentResolver = MyApplication.instance.contentResolver

        val permissionCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance, WRITE_CALENDAR
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, longEventId)
            contentResolver.delete(uri, null, null)
        }
    }

    fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }

    fun showTimePicker(clickText: TextView) {
        _showTimePicker.value = clickText
    }
}
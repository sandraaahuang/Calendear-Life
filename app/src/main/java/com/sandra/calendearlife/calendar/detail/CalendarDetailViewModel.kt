package com.sandra.calendearlife.calendar.detail

import android.app.Application
import android.content.ContentUris
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.contentResolver
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.contentValues
import com.sandra.calendearlife.constant.GoogleCalendarProvider.Companion.permissionWriteCheck
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.util.UserManager


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

    private val _hasPermission = MutableLiveData<Boolean>()
    val hasPermission: LiveData<Boolean>
        get() = _hasPermission

    init {
        _selectedItem.value = calendar
    }

    //update item
    fun updateEvent(
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
                .whereEqualTo(DOCUMENT_ID, documentID)
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
    fun deleteEvent(documentID: String) {
        _isClicked.value = true

        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .whereEqualTo(DOCUMENT_ID, documentID)
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

    fun updateGoogleEvent(eventId: String, title: String, note: String, beginDate: Timestamp, endDate: Timestamp) {
        _isClicked.value = true

        // update event
        contentValues.put(CalendarContract.Events.TITLE, title)
        contentValues.put(CalendarContract.Events.DESCRIPTION, note)
        contentValues.put(CalendarContract.Events.DTSTART, beginDate.seconds * 1000)
        contentValues.put(CalendarContract.Events.DTEND, endDate.seconds * 1000)

        if (permissionWriteCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                java.lang.Long.parseLong(eventId)
            )

            contentResolver.update(uri, contentValues, null, null)
        } else {
            _hasPermission.value = true
        }
    }

    fun deleteGoogleEvent(eventId: String) {
        _isClicked.value = true

        if (permissionWriteCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = ContentUris.withAppendedId(
                CalendarContract.Events.CONTENT_URI,
                java.lang.Long.parseLong(eventId)
            )

            contentResolver.delete(uri, null, null)
        } else {
            _hasPermission.value = true
        }
    }

    fun onButtonClick(view: View) {
        when (view.id) {
            R.id.beginDate -> showDatePicker(view.findViewById(R.id.beginDate))
            R.id.beginTime -> showTimePicker(view.findViewById(R.id.beginTime))
            R.id.endDate -> showDatePicker(view.findViewById(R.id.endDate))
            R.id.endTime -> showTimePicker(view.findViewById(R.id.endTime))
            R.id.remindDate -> showDatePicker(view.findViewById(R.id.remindDate))
            R.id.remindTime -> showTimePicker(view.findViewById(R.id.remindTime))
            R.id.targetDateInput -> showDatePicker(view.findViewById(R.id.targetDateInput))
        }
    }

    private fun showDatePicker(clickedText: TextView) {
        _showDatePicker.value = clickedText
    }

    private fun showTimePicker(clickedText: TextView) {
        _showTimePicker.value = clickedText
    }
}
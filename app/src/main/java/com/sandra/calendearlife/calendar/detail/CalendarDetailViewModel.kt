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
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*
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

        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .whereEqualTo(DOCUMENTID, documentID)
            .get()
            .addOnSuccessListener { documents ->

                for ((index, calendar) in documents.withIndex()) {

                    // update countdowns
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(COUNTDOWN)
                        .get()
                        .addOnSuccessListener { countdownQuerySnapshot ->

                            for (countdowns in countdownQuerySnapshot) {

                                // update countdowns
                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(COUNTDOWN)
                                    .document(countdowns.id)
                                    .update(countdown)
                            }
                        }

                    // update reminders
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(REMINDERS)
                        .get()
                        .addOnSuccessListener { remindersQuerySnapshot ->

                            for (reminders in remindersQuerySnapshot) {

                                // update reminders
                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(REMINDERS)
                                    .document(reminders.id)
                                    .update(updateRemind)
                            }
                        }
                    // update calendar
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .update(calendarItem)

                    if (index == documents.size() - 1) {
                        _isUpdateCompleted.value = true
                    }
                }

            }
    }


    //delete item
    fun deleteItem(documentID: String) {
        _isClicked.value = true
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .whereEqualTo(DOCUMENTID, documentID)
            .get()
            .addOnSuccessListener { calendarQuerySnapshot ->

                for ((index, calendar) in calendarQuerySnapshot.withIndex()) {

                    // delete countdowns
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(COUNTDOWN)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdowns in documents) {

                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(COUNTDOWN)
                                    .document(countdowns.id)
                                    .delete()
                            }
                        }

                    // delete reminders
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(REMINDERS)
                        .get()
                        .addOnSuccessListener { remindersQuerySnapshot ->

                            for (reminders in remindersQuerySnapshot) {

                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(REMINDERS)
                                    .document(reminders.id)
                                    .delete()
                            }
                        }


                    // delete calendar
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .delete()

                    if (index == calendarQuerySnapshot.size() - 1) {
                        _isUpdateCompleted.value = true
                    }
                }
            }


    }

    fun updateEvent(eventId: String, title: String, note: String, beginDate: Timestamp, endDate: Timestamp) {
        _isClicked.value = true

        val longEventId = java.lang.Long.parseLong(eventId)

        // update event
        val cr = MyApplication.instance.contentResolver
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
            cr.update(uri, values, null, null)
        }
    }

    fun deleteEvent(eventId: String) {
        _isClicked.value = true

        val longEventId = java.lang.Long.parseLong(eventId)

        val cr = MyApplication.instance.contentResolver

        val permissionCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance, WRITE_CALENDAR
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, longEventId)
            cr.delete(uri, null, null)
        }
    }

    fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }

    fun showTimePicker(clickText: TextView) {
        _showTimePicker.value = clickText
    }
}
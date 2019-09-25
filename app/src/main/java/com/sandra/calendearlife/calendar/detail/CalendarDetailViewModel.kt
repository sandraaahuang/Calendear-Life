package com.sandra.calendearlife.calendar.detail

import android.Manifest
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.data.Calendar
import android.provider.SyncStateContract.Helpers.update
import android.provider.CalendarContract
import android.content.ContentUris
import android.content.pm.PackageManager
import android.Manifest.permission
import android.Manifest.permission.WRITE_CALENDAR
import com.sandra.calendearlife.MainActivity
import androidx.core.content.ContextCompat
import android.content.ContentValues
import android.os.Handler
import android.widget.EditText
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import kotlinx.android.synthetic.main.activity_main.*


class CalendarDetailViewModel(calendar: Calendar, app: Application) : AndroidViewModel(app) {
    var db = FirebaseFirestore.getInstance()

    private val _selectedItem = MutableLiveData<Calendar>()

    val selectedItem: LiveData<Calendar>
        get() = _selectedItem

    private var _updateCompleted = MutableLiveData<Boolean>()

    val updateCompleted: LiveData<Boolean>
        get() = _updateCompleted

    private var _clicked = MutableLiveData<Boolean>()

    val clicked: LiveData<Boolean>
        get() = _clicked

    init {
        _selectedItem.value = calendar
    }

    //update item
    fun updateItem(documentID: String,
                   calendarItem: HashMap<String, Any>,
                   countdown: HashMap<String, Any>,
                   updateRemind: HashMap<String, Any>) {

        _clicked.value = true

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .whereEqualTo("documentID", documentID)
            .get()
            .addOnSuccessListener { documents ->

                for ((index, calendar) in documents.withIndex()) {

                    // update countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdowns in documents) {

                                // update countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdowns.id)
                                    .update(countdown)
                            }
                        }

                    // update reminders
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {

                                // update reminders
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
                                    .update(updateRemind)
                            }
                        }
                    // update calendar
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .update(calendarItem)


                    if (index == documents.size() -1) {
                        _updateCompleted.value = true
                    }
                }


            }



    }


    //delete item
    fun deleteItem(documentID: String) {
        _clicked.value = true
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .whereEqualTo("documentID", documentID)
            .get()
            .addOnSuccessListener { documents ->

                for ((index, calendar) in documents.withIndex()) {

                    // delete countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdowns in documents) {

                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdowns.id)
                                    .delete()
                            }
                        }

                    // delete reminders
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {

                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
                                    .delete()
                            }
                        }


                    // delete calendar
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .delete()

                    if (index == documents.size() -1) {
                        _updateCompleted.value = true
                    }
                }
            }


    }

    fun updateEvent(eventId: String, title: String, note: String, beginDate: Timestamp, endDate: Timestamp) {
        _clicked.value = true
        val targetEventId = eventId
        val eventId = java.lang.Long.parseLong(targetEventId)

        val targetTitle = title
        val beginDate = beginDate
        val endDate = endDate

        // update event
        val cr = MyApplication.instance.contentResolver
        val values = ContentValues()
        values.put(CalendarContract.Events.TITLE, targetTitle)
        values.put(CalendarContract.Events.DESCRIPTION, note)
        values.put(CalendarContract.Events.DTSTART, beginDate.seconds*1000)
        values.put(CalendarContract.Events.DTEND, endDate.seconds*1000)

        val permissionCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance, WRITE_CALENDAR)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            cr.update(uri, values, null, null)
        }
    }

    fun deleteEvent(eventId: String) {
        _clicked.value = true
        val targetEventId = eventId
        val eventId = java.lang.Long.parseLong(targetEventId)

        val cr = MyApplication.instance.contentResolver

        val permissionCheck = ContextCompat.checkSelfPermission(
            MyApplication.instance, WRITE_CALENDAR
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            cr.delete(uri, null, null)
        }
    }
}
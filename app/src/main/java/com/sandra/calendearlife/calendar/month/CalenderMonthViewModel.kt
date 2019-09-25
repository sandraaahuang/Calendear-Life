package com.sandra.calendearlife.calendar.month

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.dialog.RepeatDialog
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalenderMonthViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val simpleDateTimeFormat = SimpleDateFormat("yyyy-MM-dd h:mm a")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    lateinit var calenderAdd: com.sandra.calendearlife.data.Calendar

    val _liveCalendar = MutableLiveData<List<com.sandra.calendearlife.data.Calendar>>()
    val liveCalendar: LiveData<List<com.sandra.calendearlife.data.Calendar>>
        get() = _liveCalendar

    val _liveAllCalendar = MutableLiveData<List<com.sandra.calendearlife.data.Calendar>>()
    val liveAllCalendar: LiveData<List<com.sandra.calendearlife.data.Calendar>>
        get() = _liveAllCalendar

    private val _navigateToCalendarProperty = MutableLiveData<com.sandra.calendearlife.data.Calendar>()

    val navigateToCalendarProperty: LiveData<com.sandra.calendearlife.data.Calendar>
        get() = _navigateToCalendarProperty

    fun displayCalendarDetails(calendar: com.sandra.calendearlife.data.Calendar) {
        _navigateToCalendarProperty.value = calendar
    }

    fun displayCalendarDetailsComplete() {
        _navigateToCalendarProperty.value = null
    }

    init {
        queryAll()
    }

    // get user's today calendar
    fun queryToday(today: Timestamp) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { documents ->
                // put today calendar into recyclerView
                val calendarItem = ArrayList<com.sandra.calendearlife.data.Calendar>()
                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // need to transfer date from timestamp into string
                    val date = (calendar.data["date"] as Timestamp)
                    val setDate = (calendar.data["setDate"] as Timestamp)
                    val beginDate = (calendar.data["beginDate"] as Timestamp)
                    val endDate = (calendar.data["endDate"] as Timestamp)


                    calenderAdd = com.sandra.calendearlife.data.Calendar(
                        calendar.data["color"].toString(),
                        simpleDateFormat.format(date.seconds * 1000),
                        simpleDateFormat.format(setDate.seconds * 1000),
                        simpleDateTimeFormat.format(beginDate.seconds * 1000),
                        simpleDateTimeFormat.format(endDate.seconds * 1000),
                        calendar.data["title"].toString(),
                        calendar.data["note"].toString(),
                        calendar.data["isAllDay"].toString().toBoolean(),
                        calendar.data["hasLocation"].toString().toBoolean(),
                        calendar.data["location"].toString(),
                        calendar.data["hasReminders"].toString().toBoolean(),
                        calendar.data["hasCountdown"].toString().toBoolean(),
                        calendar.data["documentID"].toString(),
                        calendar.data["frequency"].toString(),
                        calendar.data["fromGoogle"].toString().toBoolean()
                    )
                    calendarItem.add(calenderAdd)

                }
                _liveCalendar.value = calendarItem
            }
    }

    fun queryAll() {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->
                // put today calendar into recyclerView
                val calendarItem = ArrayList<com.sandra.calendearlife.data.Calendar>()
                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // need to transfer date from timestamp into string
                    val date = (calendar.data["date"] as Timestamp)
                    val setDate = (calendar.data["setDate"] as Timestamp)
                    val beginDate = (calendar.data["beginDate"] as Timestamp)
                    val endDate = (calendar.data["endDate"] as Timestamp)


                    calenderAdd = com.sandra.calendearlife.data.Calendar(
                        calendar.data["color"].toString(),
                        simpleDateFormat.format(date.seconds * 1000),
                        simpleDateFormat.format(setDate.seconds * 1000),
                        simpleDateTimeFormat.format(beginDate.seconds * 1000),
                        simpleDateTimeFormat.format(endDate.seconds * 1000),
                        calendar.data["title"].toString(),
                        calendar.data["note"].toString(),
                        calendar.data["isAllDay"].toString().toBoolean(),
                        calendar.data["hasLocation"].toString().toBoolean(),
                        calendar.data["location"].toString(),
                        calendar.data["hasReminders"].toString().toBoolean(),
                        calendar.data["hasCountdown"].toString().toBoolean(),
                        calendar.data["documentID"].toString(),
                        calendar.data["frequency"].toString(),
                        calendar.data["fromGoogle"].toString().toBoolean()
                    )
                    calendarItem.add(calenderAdd)

                }
                _liveAllCalendar.value = calendarItem
            }
    }
}
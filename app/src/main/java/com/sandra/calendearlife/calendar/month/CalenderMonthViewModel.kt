package com.sandra.calendearlife.calendar.month

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.UserManager
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalenderMonthViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    lateinit var calenderAdd: com.sandra.calendearlife.data.Calendar

    val _allCalendar = MutableLiveData<List<com.sandra.calendearlife.data.Calendar>>()
    val allCalendar: LiveData<List<com.sandra.calendearlife.data.Calendar>>
        get() = _allCalendar


    val _liveCalendar = MutableLiveData<List<com.sandra.calendearlife.data.Calendar>>()
    val liveCalendar: LiveData<List<com.sandra.calendearlife.data.Calendar>>
        get() = _liveCalendar

//    init {
//        getCalendar()
//    }

    fun getCalendar() {

        // get user data
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->
                val calendarItem = ArrayList<com.sandra.calendearlife.data.Calendar>()
                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    val date = (calendar.data["date"] as Timestamp)
                    val setDate = (calendar.data["setDate"] as Timestamp)
                    val beginDate = (calendar.data["beginDate"] as Timestamp)
                    val endDate = (calendar.data["endDate"] as Timestamp)

                    calenderAdd = com.sandra.calendearlife.data.Calendar(
                        simpleDateFormat.format(date.seconds * 1000),
                        simpleDateFormat.format(setDate.seconds * 1000),
                        simpleDateFormat.format(beginDate.seconds * 1000),
                        simpleDateFormat.format(endDate.seconds * 1000),
                        calendar.data["title"].toString(),
                        calendar.data["note"].toString(),
                        calendar.data["hasGuests"].toString().toBoolean(),
                        null,
                        calendar.data["isAllDay"].toString().toBoolean(),
                        calendar.data["hasLocation"].toString().toBoolean(),
                        calendar.data["location"].toString(),
                        calendar.data["hasReminders"].toString().toBoolean(),
                        calendar.data["hasCountdown"].toString().toBoolean(),
                        null
                    )
                    calendarItem.add(calenderAdd)
                    _allCalendar.value = calendarItem
                    Log.d("sandraaa", "data = ${liveCalendar.value}")
                }


            }
    }

    // get user's today calendar
    fun queryToday(today: Timestamp) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { documents ->
                val calendarItem = ArrayList<com.sandra.calendearlife.data.Calendar>()
                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    val date = (calendar.data["date"] as Timestamp)
                    val setDate = (calendar.data["setDate"] as Timestamp)
                    val beginDate = (calendar.data["beginDate"] as Timestamp)
                    val endDate = (calendar.data["endDate"] as Timestamp)


                    calenderAdd = com.sandra.calendearlife.data.Calendar(
                        simpleDateFormat.format(date.seconds * 1000),
                        simpleDateFormat.format(setDate.seconds * 1000),
                        simpleDateFormat.format(beginDate.seconds * 1000),
                        simpleDateFormat.format(endDate.seconds * 1000),
                        calendar.data["title"].toString(),
                        calendar.data["note"].toString(),
                        calendar.data["hasGuests"].toString().toBoolean(),
                        null,
                        calendar.data["isAllDay"].toString().toBoolean(),
                        calendar.data["hasLocation"].toString().toBoolean(),
                        calendar.data["location"].toString(),
                        calendar.data["hasReminders"].toString().toBoolean(),
                        calendar.data["hasCountdown"].toString().toBoolean(),
                        null
                    )
                    calendarItem.add(calenderAdd)

                }
                _liveCalendar.value = calendarItem
                Log.d("sandraaa", "data = ${liveCalendar.value}")
            }
    }
}
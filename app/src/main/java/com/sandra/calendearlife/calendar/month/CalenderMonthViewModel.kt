package com.sandra.calendearlife.calendar.month

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ENDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROMGOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASCOUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASLOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASREMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ISALLDAY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalenderMonthViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private val locale: Locale =
        if (Locale.getDefault().toString() == "zh-rtw") {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", locale)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

    private lateinit var calenderAdd: com.sandra.calendearlife.data.Calendar
    private val _liveCalendar = MutableLiveData<List<com.sandra.calendearlife.data.Calendar>>()
    val liveCalendar: LiveData<List<com.sandra.calendearlife.data.Calendar>>
        get() = _liveCalendar

    private val _liveAllCalendar = MutableLiveData<List<com.sandra.calendearlife.data.Calendar>>()
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

        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .whereEqualTo(DATE, today)
            .get()
            .addOnSuccessListener { documents ->
                // put today calendar into recyclerView
                val calendarItem = ArrayList<com.sandra.calendearlife.data.Calendar>()
                for (calendar in documents) {

                    // need to transfer date from timestamp into string
                    val date = (calendar.data[DATE] as Timestamp)
                    val setDate = (calendar.data[SETDATE] as Timestamp)
                    val beginDate = (calendar.data[BEGINDATE] as Timestamp)
                    val endDate = (calendar.data[ENDDATE] as Timestamp)

                    calenderAdd = com.sandra.calendearlife.data.Calendar(
                        calendar.data[COLOR].toString(),
                        simpleDateFormat.format(date.seconds * 1000),
                        simpleDateFormat.format(setDate.seconds * 1000),
                        dateTimeFormat.format(beginDate.seconds * 1000),
                        dateTimeFormat.format(endDate.seconds * 1000),
                        calendar.data[TITLE].toString(),
                        calendar.data[NOTE].toString(),
                        calendar.data[ISALLDAY].toString().toBoolean(),
                        calendar.data[HASLOCATION].toString().toBoolean(),
                        calendar.data[LOCATION].toString(),
                        calendar.data[HASREMINDERS].toString().toBoolean(),
                        calendar.data[HASCOUNTDOWN].toString().toBoolean(),
                        calendar.data[DOCUMENTID].toString(),
                        calendar.data[FREQUENCY].toString(),
                        calendar.data[FROMGOOGLE].toString().toBoolean()
                    )
                    calendarItem.add(calenderAdd)

                }
                _liveCalendar.value = calendarItem
            }
    }

    private fun queryAll() {

        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->
                // put today calendar into recyclerView
                val calendarItem = ArrayList<com.sandra.calendearlife.data.Calendar>()
                for (calendar in documents) {

                    // need to transfer date from timestamp into string
                    val date = (calendar.data[DATE] as Timestamp)
                    val setDate = (calendar.data[SETDATE] as Timestamp)
                    val beginDate = (calendar.data[BEGINDATE] as Timestamp)
                    val endDate = (calendar.data[ENDDATE] as Timestamp)


                    calenderAdd = com.sandra.calendearlife.data.Calendar(
                        calendar.data[COLOR].toString(),
                        simpleDateFormat.format(date.seconds * 1000),
                        simpleDateFormat.format(setDate.seconds * 1000),
                        dateTimeFormat.format(beginDate.seconds * 1000),
                        dateTimeFormat.format(endDate.seconds * 1000),
                        calendar.data[TITLE].toString(),
                        calendar.data[NOTE].toString(),
                        calendar.data[ISALLDAY].toString().toBoolean(),
                        calendar.data[HASLOCATION].toString().toBoolean(),
                        calendar.data[LOCATION].toString(),
                        calendar.data[HASREMINDERS].toString().toBoolean(),
                        calendar.data[HASCOUNTDOWN].toString().toBoolean(),
                        calendar.data[DOCUMENTID].toString(),
                        calendar.data[FREQUENCY].toString(),
                        calendar.data[FROMGOOGLE].toString().toBoolean()
                    )
                    calendarItem.add(calenderAdd)

                }
                _liveAllCalendar.value = calendarItem
            }
    }
}
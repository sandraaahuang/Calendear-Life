package com.sandra.calendearlife.calendar.month

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.sandra.calendearlife.addCalendarItem
import com.sandra.calendearlife.constant.DATE_TIME_FORMAT
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGIN_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.END_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROM_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_ALL_DAY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SIMPLE_DATE_FORMAT
import com.sandra.calendearlife.constant.transferTimestamp2String
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.util.UserManager

class CalenderMonthViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private lateinit var calendarAdd: Calendar
    private val _liveCalendar = MutableLiveData<List<Calendar>>()
    val liveCalendar: LiveData<List<Calendar>>
        get() = _liveCalendar

    private val _liveAllCalendar = MutableLiveData<List<Calendar>>()
    val liveAllCalendar: LiveData<List<Calendar>>
        get() = _liveAllCalendar

    private val _navigateToCalendarProperty = MutableLiveData<Calendar>()

    val navigateToCalendarProperty: LiveData<Calendar>
        get() = _navigateToCalendarProperty

    fun displayCalendarDetails(calendar: Calendar) {
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

        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .whereEqualTo(DATE, today)
                .get()
                .addOnSuccessListener { documents ->
                    // put today calendar into recyclerView
                    val calendarItem = ArrayList<Calendar>()
                    for (calendar in documents) {

                        addCalendarItem(calendar, calendarItem)

                    }
                    _liveCalendar.value = calendarItem
                }
        }
    }

    private fun queryAll() {

        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->
                    // put today calendar into recyclerView
                    val calendarItem = ArrayList<Calendar>()
                    for (calendar in documents) {

                        addCalendarItem(calendar, calendarItem)

                    }
                    _liveAllCalendar.value = calendarItem
                }
        }
    }
}
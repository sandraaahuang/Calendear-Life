package com.sandra.calendearlife.calendar.month

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.getCalendarItemFromFirebase
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.util.UserManager

class CalenderMonthViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

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

                        getCalendarItemFromFirebase(calendar, calendarItem)

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

                        getCalendarItemFromFirebase(calendar, calendarItem)

                    }
                    _liveAllCalendar.value = calendarItem
                }
        }
    }
}
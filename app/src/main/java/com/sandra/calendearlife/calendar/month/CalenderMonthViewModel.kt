package com.sandra.calendearlife.calendar.month

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalenderMonthViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    lateinit var calenderAdd: com.sandra.calendearlife.data.Calendar

    val calendarItem = ArrayList<com.sandra.calendearlife.data.Calendar>()
    val _liveCalendar = MutableLiveData<List<com.sandra.calendearlife.data.Calendar>>()
    val liveCalendar: LiveData<List<com.sandra.calendearlife.data.Calendar>>
        get() = _liveCalendar

    fun getCalendar() {

        db.collection("data")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (data in task.result!!) {
                        Log.d("getAllDate", data.id + " => " + data.data)

                        db.collection("data")
                            .document(data.id)
                            .collection("calendar")
                            .get()
                            .addOnSuccessListener { documents ->

                                for (calendar in documents) {
                                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                                    val setDate = (calendar.data["setDate"] as com.google.firebase.Timestamp)
                                    val beginDate = (calendar.data["beginDate"] as com.google.firebase.Timestamp)
                                    val endDate = (calendar.data["endDate"] as com.google.firebase.Timestamp)

                                    calenderAdd = com.sandra.calendearlife.data.Calendar(
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
            }
    }


}
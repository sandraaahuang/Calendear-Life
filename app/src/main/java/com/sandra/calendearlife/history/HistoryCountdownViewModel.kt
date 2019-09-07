package com.sandra.calendearlife.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryCountdownViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    lateinit var countdownAdd: Countdown

    val countdownItem = ArrayList<Countdown>()
    val _liveCountdown = MutableLiveData<List<Countdown>>()
    val liveCountdown: LiveData<List<Countdown>>
        get() = _liveCountdown

    init {
        getItem()
    }

    fun getItem() {
        //connect to countdown data ( only the item that overdue is true )
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // get countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .whereEqualTo("overdue", true)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {
                                Log.d("getAllcountdown", "${countdown.id} => ${countdown.data}")
                                val setDate = (countdown.data["setDate"] as Timestamp)
                                val targetDate = (countdown.data["targetDate"] as Timestamp)

                                countdownAdd = Countdown(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    countdown.data["title"].toString(),
                                    countdown.data["note"].toString(),
                                    simpleDateFormat.format(targetDate.seconds * 1000),
                                    countdown.data["targetDate"] as Timestamp,
                                    countdown.data["overdue"].toString().toBoolean(),
                                    countdown.data["documentID"].toString()
                                )

                                countdownItem.add(countdownAdd)
                            }

                            _liveCountdown.value = countdownItem

                        }
                }
            }
    }
}
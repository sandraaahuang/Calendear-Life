package com.sandra.calendearlife.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)


    lateinit var countdownAdd: Countdown
    lateinit var remindAdd: Reminders

    val countdownItem = ArrayList<Countdown>()
    val _liveCountdown = MutableLiveData<List<Countdown>>()
    val liveCountdown: LiveData<List<Countdown>>
        get() = _liveCountdown

    val remindersItem = ArrayList<Reminders>()
    val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders : LiveData<List<Reminders>>
        get() = _liveReminders

    init {
        getItem()
    }

    fun getItem(){
    //connect to countdown data
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

                            // get countdowns
                            db.collection("data")
                                .document(data.id)
                                .collection("calendar")
                                .document(calendar.id)
                                .collection("countdowns")
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (countdown in documents) {
                                        Log.d("getAllcountdown", "${countdown.id} => ${countdown.data}")
                                        val setDate = (countdown.data["setDate"]as com.google.firebase.Timestamp)
                                        val targetDate = (countdown.data["targetDate"]as com.google.firebase.Timestamp)

                                        countdownAdd = Countdown(
                                            simpleDateFormat.format(setDate.seconds*1000),
                                            countdown.data["title"].toString(),
                                            countdown.data["note"].toString(),
                                            simpleDateFormat.format(targetDate.seconds*1000),
                                            countdown.data["overdue"].toString().toBoolean())

                                        countdownItem.add(countdownAdd)
                                    }

                                    _liveCountdown.value = countdownItem
                                }

                                .addOnFailureListener { exception ->
                                    Log.w("getAllcountdown", "Error getting documents: ", exception)
                                }

                            //get reminders
                            db.collection("data")
                                .document(data.id)
                                .collection("calendar")
                                .document(calendar.id)
                                .collection("reminders")
                                .get()
                                .addOnSuccessListener { documents ->

                                    for (reminder in documents) {
                                        Log.d("getAllreminders", "${reminder.id} => ${reminder.data}")

                                        val setDate = (reminder.data["setDate"]as com.google.firebase.Timestamp)
                                        val remindDate = (reminder.data["remindDate"]as com.google.firebase.Timestamp)

                                        remindAdd = Reminders(
                                            simpleDateFormat.format(setDate.seconds*1000),
                                            reminder.data["title"].toString(),
                                            reminder.data["setRemindDate"].toString().toBoolean(),
                                            simpleDateFormat.format(remindDate.seconds*1000),
                                            reminder.data["isChecked"].toString().toBoolean(),
                                            reminder.data["note"].toString(),
                                            reminder.data["frequency"].toString())

                                        remindersItem.add(remindAdd)
                                    }
                                    _liveReminders.value = remindersItem
                                }

                                .addOnFailureListener { exception ->
                                    Log.w("getAllreminders", "Error getting documents: ", exception)
                                }
                        }
                    }

                    .addOnFailureListener { exception ->
                        Log.w("getAllCalendar", "Error getting documents: ", exception)
                    }

            }
        } else {
            Log.w("getAllDate", "Error getting documents.", task.exception)
        }
    }
}
}
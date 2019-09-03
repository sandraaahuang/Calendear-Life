package com.sandra.calendearlife.reminders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Reminders
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RemindersViewModel : ViewModel(){
    var db = FirebaseFirestore.getInstance()

    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    lateinit var remindAdd: Reminders

    val remindersItem = ArrayList<Reminders>()
    val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders : LiveData<List<Reminders>>
        get() = _liveReminders

    init {
        getItem()
    }

    private val _navigateToReminderProperty = MutableLiveData<Reminders>()

    val navigateToReminderProperty: LiveData<Reminders>
        get() = _navigateToReminderProperty

    fun displayReminderDetails(reminders: Reminders) {
        _navigateToReminderProperty.value = reminders
    }

    fun displayReminderDetailsComplete() {
        _navigateToReminderProperty.value = null
    }

    fun writeItem(item: Any){

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

                                    // add countdowns
                                    db.collection("data")
                                        .document(data.id)
                                        .collection("calendar")
                                        .document(calendar.id)
                                        .collection("reminders")
                                        .add(item)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d(
                                                "AddCountdownsIntoDB",
                                                "DocumentSnapshot added with ID: " + documentReference.id
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("AddCountdownsIntoDB", "Error adding document", e)
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

                                    // get reminders

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
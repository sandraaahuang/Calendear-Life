package com.sandra.calendearlife.reminders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders

class RemindersViewModel : ViewModel(){
    var db = FirebaseFirestore.getInstance()

    lateinit var remindAdd: Reminders

    val remindersItem = ArrayList<Reminders>()
    val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders : LiveData<List<Reminders>>
        get() = _liveReminders

    init {
        getItem()
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

                                                remindAdd = reminder.toObject(Reminders::class.java)

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
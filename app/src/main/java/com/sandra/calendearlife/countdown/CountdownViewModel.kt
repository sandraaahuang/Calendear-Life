package com.sandra.calendearlife.countdown

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders

class CountdownViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    lateinit var countdownAdd: Countdown

    val countdownItem = ArrayList<Countdown>()
    val _liveCountdown = MutableLiveData<List<Countdown>>()
    val liveCountdown: LiveData<List<Countdown>>
        get() = _liveCountdown

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

                                    // get countdowns
                                    db.collection("data")
                                        .document(data.id)
                                        .collection("calendar")
                                        .document(calendar.id)
                                        .collection("countdowns")
                                        .add(item)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d(
                                                "AddArticleIntoDataBase",
                                                "DocumentSnapshot added with ID: " + documentReference.id
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("AddArticleIntoDataBase", "Error adding document", e)
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


    fun getItem() {
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
                                                countdownAdd = countdown.toObject(Countdown::class.java)

                                                countdownItem.add(countdownAdd)
                                            }

                                            _liveCountdown.value = countdownItem
                                        }

                                        .addOnFailureListener { exception ->
                                            Log.w("getAllcountdown", "Error getting documents: ", exception)
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
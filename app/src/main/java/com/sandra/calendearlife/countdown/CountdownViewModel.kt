package com.sandra.calendearlife.countdown

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Countdown
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CountdownViewModel : ViewModel() {
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

    private val _navigateToCountdownProperty = MutableLiveData<Countdown>()

    val navigateToCountdownProperty: LiveData<Countdown>
        get() = _navigateToCountdownProperty

    fun displayCountdownDetails(countdown: Countdown) {
        _navigateToCountdownProperty.value = countdown
    }

    fun displayCountdownDetailsComplete() {
        _navigateToCountdownProperty.value = null
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
                                        .collection("countdowns")
                                        .add(item)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d(
                                                "AddcountdownsIntoDB",
                                                "DocumentSnapshot added with ID: " + documentReference.id
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("AddcountdownsIntoDB", "Error adding document", e)
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

    fun Date.getStringTimeStampWithDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(this)
    }
}
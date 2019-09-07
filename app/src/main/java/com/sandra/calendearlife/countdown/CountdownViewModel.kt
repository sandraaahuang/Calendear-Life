package com.sandra.calendearlife.countdown

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager
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

    fun writeItem(calendar: Any, countdown: Any) {
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .add(calendar)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "AddCalendarIntoDB",
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .document(documentReference.id)
                    .update("documentID", documentReference.id)

                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .document(documentReference.id)
                    .collection("countdowns")
                    .add(countdown)
                    .addOnSuccessListener { countdownID ->
                        Log.d(
                            "AddCountdownsIntoDB",
                            "DocumentSnapshot added with ID: " + countdownID.id
                        )
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .document(documentReference.id)
                            .collection("countdowns")
                            .document(countdownID.id)
                            .update("documentID", countdownID.id)
                    }
            }
    }


    fun getItem() {
        //connect to countdown data
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
                        .whereEqualTo("overdue", false)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {
                                Log.d("getAllcountdown", "${countdown.id} => ${countdown.data}")
                                val setDate =
                                    (countdown.data["setDate"] as com.google.firebase.Timestamp)
                                val targetDate =
                                    (countdown.data["targetDate"] as com.google.firebase.Timestamp)

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

                        .addOnFailureListener { exception ->
                            Log.w("getAllcountdown", "Error getting documents: ", exception)
                        }
                }
            }

            .addOnFailureListener { exception ->
                Log.w("getAllCalendar", "Error getting documents: ", exception)
            }
    }

    //update countdown item to overdue depends on time
    fun updateItem(documentId: String) {
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // add countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {
                                Log.d("getAllCalendar", "${reminders.id} => ${reminders.data}")

                                // add countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(documentId)
                                    .update("overdue", true)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "RenewCountdown",
                                            "successfully updated my status!"
                                        )
                                    }
                            }
                        }
                }
            }
    }
}

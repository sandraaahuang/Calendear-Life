package com.sandra.calendearlife.reminders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.data.Reminders
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RemindersViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    lateinit var remindAdd: Reminders

    val remindersItem = ArrayList<Reminders>()
    val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders: LiveData<List<Reminders>>
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

    fun writeItem(calendar: Any, reminder: Any) {
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
                    .collection("reminders")
                    .add(reminder)
                    .addOnSuccessListener { reminderID ->
                        Log.d(
                            "AddCountdownsIntoDB",
                            "DocumentSnapshot added with ID: " + reminderID.id
                        )
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .document(documentReference.id)
                            .collection("reminders")
                            .document(reminderID.id)
                            .update("documentID", reminderID.id)
                    }
            }
    }


    // also only get isChecked is false
    fun getItem() {
        //connect to countdown data
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // get reminders

                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("isChecked", false)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminder in documents) {
                                Log.d("getAllreminders", "${reminder.id} => ${reminder.data}")

                                val setDate =
                                    (reminder.data["setDate"] as com.google.firebase.Timestamp)
                                val remindDate =
                                    (reminder.data["remindDate"] as com.google.firebase.Timestamp)

                                remindAdd = Reminders(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    reminder.data["title"].toString(),
                                    reminder.data["setRemindDate"].toString().toBoolean(),
                                    simpleDateFormat.format(remindDate.seconds * 1000),
                                    reminder.data["remindDate"] as com.google.firebase.Timestamp,
                                    reminder.data["isChecked"].toString().toBoolean(),
                                    reminder.data["note"].toString(),
                                    reminder.data["frequency"].toString(),
                                    reminder.data["documentID"].toString()
                                )

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

    // update isChecked to true when user click the button
    fun updateItem(documentID: String) {

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
                        .collection("reminders")
                        .whereEqualTo("documentID", documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {
                                Log.d("getAllCalendar", "${reminders.id} => ${reminders.data}")

                                // add countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(documentID)
                                    .update("isChecked", true)
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

    // delete item due to swipe specific item
    fun deleteItem(title: String) {

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
                        .collection("reminders")
                        .whereEqualTo("title", title)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {
                                Log.d("getAllCalendar", "${reminders.id} => ${reminders.data}")

                                // add countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "delete",
                                            "successfully updated my status!"
                                        )
                                    }

                                // delete calendar
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "delete",
                                            "successfully updated my status!"
                                        )
                                    }
                            }
                        }
                }
            }
    }
}

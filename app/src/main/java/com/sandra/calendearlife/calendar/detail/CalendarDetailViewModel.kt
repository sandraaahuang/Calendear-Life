package com.sandra.calendearlife.calendar.detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.UserManager
import com.sandra.calendearlife.data.Calendar

class CalendarDetailViewModel(calendar: Calendar, app: Application) : AndroidViewModel(app) {
    var db = FirebaseFirestore.getInstance()

    private val _selectedItem = MutableLiveData<Calendar>()

    val selectedItem: LiveData<Calendar>
        get() = _selectedItem

    init {
        _selectedItem.value = calendar
    }

    //update item
    fun updateItem(documentID: String,
                   calendarItem: HashMap<String, Any>,
                   countdown: HashMap<String, Any>,
                   updateRemind: HashMap<String, Any>) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .whereEqualTo("documentID", documentID)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // update countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdowns in documents) {
                                Log.d("getAllCalendar", "${countdowns.id} => ${countdowns.data}")

                                // update countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdowns.id)
                                    .update(countdown)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "update countdown",
                                            "id = $documentID"
                                        )
                                    }

                            }
                        }

                    // update reminders
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {
                                Log.d("getAllReminders", "${reminders.id} => ${reminders.data}")

                                // update reminders
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
                                    .update(updateRemind)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "RenewCountdown",
                                            "update reminders"
                                        )
                                    }

                            }
                        }
                    // update calendar
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .update(calendarItem)
                        .addOnSuccessListener {
                            Log.d(
                                "update calendar",
                                "id = $documentID"
                            )
                        }
                }
            }


    }


    //delete item
    fun deleteItem(documentID: String) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .whereEqualTo("documentID", documentID)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // delete countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdowns in documents) {
                                Log.d("getAllCalendar", "${countdowns.id} => ${countdowns.data}")

                                // add countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdowns.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "delete countdown",
                                            "id = $documentID"
                                        )
                                    }

                            }
                        }

                    // delete reminders
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
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
                                            "RenewCountdown",
                                            "delete reminders"
                                        )
                                    }

                            }
                        }


                    // delete calendar
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(
                                "delete calendar",
                                "id = $documentID"
                            )
                        }


                }
            }


    }
}
package com.sandra.calendearlife.calendar.detail

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.util.UserManager
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
                                Log.d("getAllCountdownById", "${countdowns.id} => ${countdowns.data}")

                                // update countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdowns.id)
                                    .update(countdown)
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
                                Log.d("getAllRemindersById", "${reminders.id} => ${reminders.data}")

                                // update reminders
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
                                    .update(updateRemind)
                            }
                        }
                    // update calendar
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .update(calendarItem)
                        .addOnSuccessListener {
                            Toast.makeText(MyApplication.instance, "Successfully updated", Toast.LENGTH_SHORT)
                                .show()
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
                                Log.d("getAllCountdown", "${countdowns.id} => ${countdowns.data}")

                                // add countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdowns.id)
                                    .delete()
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
                                Log.d("getAllRemindersById", "${reminders.id} => ${reminders.data}")

                                // add countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
                                    .delete()
                            }
                        }


                    // delete calendar
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(MyApplication.instance, "Successfully deleted", Toast.LENGTH_SHORT)
                                .show()
                        }


                }
            }


    }
}
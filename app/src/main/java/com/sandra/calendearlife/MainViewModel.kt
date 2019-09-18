package com.sandra.calendearlife

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel : ViewModel() {

    var db = FirebaseFirestore.getInstance()
    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
    val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd h:mm a")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    // Record current fragment to support data binding
    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    val userEmail = UserManager.userEmail
    val userPhoto = UserManager.userPhoto

    lateinit var remindAdd: Reminders
    val _liveReminders = MutableLiveData<Reminders>()
    val liveReminders: LiveData<Reminders>
        get() = _liveReminders

    val dnrItem = ArrayList<Reminders>()
    val _livednr = MutableLiveData<List<Reminders>>()
    val livednr: LiveData<List<Reminders>>
        get() = _livednr

    fun getItem(documentId: String) {
        //connect to countdown data ( only the item that overdue is false )
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
//                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    //get reminders ( only ischecked is false )
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("documentID", documentId)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminder in documents) {
                                Log.d("getAllreminders", "${reminder.id} => ${reminder.data}")

                                val setDate = (reminder.data["setDate"] as Timestamp)
                                val remindDate = (reminder.data["remindDate"] as Timestamp)

                                remindAdd = Reminders(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    reminder.data["title"].toString(),
                                    reminder.data["setRemindDate"].toString().toBoolean(),
                                    dateTimeFormat.format(remindDate.seconds * 1000),
                                    reminder.data["remindDate"] as Timestamp,
                                    reminder.data["isChecked"].toString().toBoolean(),
                                    reminder.data["note"].toString(),
                                    reminder.data["frequency"].toString(),
                                    reminder.data["documentID"].toString()
                                )
                                _liveReminders.value = remindAdd
                            }

                            Log.d("sandraaa", "reminder = ${liveReminders.value}")

                        }
                }
            }
    }

    fun dnrItem() {
        //connect to countdown data ( only the item that overdue is false )
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    //get reminders ( only ischecked is false )
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

                                val setDate = (reminder.data["setDate"] as Timestamp)
                                val remindDate = (reminder.data["remindDate"] as Timestamp)

                                remindAdd = Reminders(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    reminder.data["title"].toString(),
                                    reminder.data["setRemindDate"].toString().toBoolean(),
                                    dateTimeFormat.format(remindDate.seconds * 1000),
                                    reminder.data["remindDate"] as Timestamp,
                                    reminder.data["isChecked"].toString().toBoolean(),
                                    reminder.data["note"].toString(),
                                    reminder.data["frequency"].toString(),
                                    reminder.data["documentID"].toString()
                                )

                                dnrItem.add(remindAdd)
                            }
                            _livednr.value = dnrItem
                            Log.d("sandraaa", "liveDate=  ${livednr.value}")
                        }
                }
            }
    }
}

package com.sandra.calendearlife.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.DateFormat.Companion.simpleDateFormat
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager
import kotlin.collections.ArrayList

class HistoryRemindersViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    lateinit var remindAdd: Reminders

    private val remindersItem = ArrayList<Reminders>()
    private val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders: LiveData<List<Reminders>>
        get() = _liveReminders

    init {
        getItem()
    }

    fun getItem() {
        //connect to countdown data ( only the item that overdue is false )
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    //get reminders ( only ischecked is true )
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("isChecked", true)
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
                                    simpleDateFormat.format(remindDate.seconds * 1000),
                                    reminder.data["remindDate"] as Timestamp,
                                    reminder.data["isChecked"].toString().toBoolean(),
                                    reminder.data["note"].toString(),
                                    reminder.data["frequency"].toString(),
                                    reminder.data["documentID"].toString()
                                )

                                remindersItem.add(remindAdd)
                            }
                            _liveReminders.value = remindersItem

                        }
                }
            }
    }
}
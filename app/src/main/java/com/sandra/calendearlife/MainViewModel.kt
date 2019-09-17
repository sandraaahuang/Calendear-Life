package com.sandra.calendearlife

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.calendar.notification.ReminderWorker
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

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

    val remindersItem = ArrayList<Reminders>()
    val _liveRemindersDnr = MutableLiveData<List<Reminders>>()
    val liveRemindersDnr: LiveData<List<Reminders>>
        get() = _liveRemindersDnr


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

    init {
        dnrItem()
    }

    fun dnrItem() {
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
                        .whereEqualTo("isChecked", false)
                        .whereEqualTo("setRemindDate", true)
                        .whereEqualTo("frequency", "Does not repeat")
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminder in documents) {
                                Log.d("getDnrreminders", "${reminder.id} => ${reminder.data}")

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
                                remindersItem.add(remindAdd)
                            }
                            _liveRemindersDnr.value = remindersItem

                            liveRemindersDnr.value?.let {
                                for ((index, value) in it.withIndex()) {
                                    val reminderDnrRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                                        .setInitialDelay(
                                            ((value.remindTimestamp.seconds - com.google.firebase.Timestamp.now().seconds)),
                                            TimeUnit.SECONDS
                                        )
                                        .build()

                                    WorkManager.getInstance()
                                        .enqueue(reminderDnrRequest)

                                    Log.d(
                                        "workmanager",
                                        "${value.remindTimestamp.seconds}, ${com.google.firebase.Timestamp.now().seconds}"
                                    )
                                }
                            }

                        }
                }
            }
    }
}

package com.sandra.calendearlife.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ISCHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETREMINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    val locale: Locale =
        if (Locale.getDefault().toString() == "zh-rtw") {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", locale)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

    lateinit var countdownAdd: Countdown
    private val countdownItem = ArrayList<Countdown>()
    private val _liveCountdown = MutableLiveData<List<Countdown>>()
    val liveCountdown: LiveData<List<Countdown>>
        get() = _liveCountdown

    lateinit var remindAdd: Reminders
    val remindersItem = ArrayList<Reminders>()
    private val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders: LiveData<List<Reminders>>
        get() = _liveReminders

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean>
        get() = _isRefreshing

    init {
        getItem()
    }

    fun getItem() {
        //connect to countdown data ( only the item that overdue is false )
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // get countdowns
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(COUNTDOWN)
                        .whereEqualTo(OVERDUE, false)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {

                                val setDate = (countdown.data[SETDATE] as Timestamp)
                                val targetDate = (countdown.data[TARGETDATE] as Timestamp)

                                countdownAdd = Countdown(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    countdown.data[TITLE].toString(),
                                    countdown.data[NOTE].toString(),
                                    simpleDateFormat.format(targetDate.seconds * 1000),
                                    countdown.data[TARGETDATE] as Timestamp,
                                    countdown.data[OVERDUE].toString().toBoolean(),
                                    countdown.data[DOCUMENTID].toString()
                                )

                                countdownItem.add(countdownAdd)
                            }

                            _liveCountdown.value = countdownItem

                        }

                    //get reminders ( only ischecked is false )
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(REMINDERS)
                        .whereEqualTo(ISCHECKED, false)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminder in documents) {

                                val setDate = (reminder.data[SETDATE] as Timestamp)
                                val remindDate = (reminder.data[REMINDDATE] as Timestamp)

                                remindAdd = Reminders(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    reminder.data[TITLE].toString(),
                                    reminder.data[SETREMINDATE].toString().toBoolean(),
                                    dateTimeFormat.format(remindDate.seconds * 1000),
                                    reminder.data[REMINDDATE] as Timestamp,
                                    reminder.data[ISCHECKED].toString().toBoolean(),
                                    reminder.data[NOTE].toString(),
                                    reminder.data[FREQUENCY].toString(),
                                    reminder.data[DOCUMENTID].toString()
                                )

                                remindersItem.add(remindAdd)
                            }
                            _liveReminders.value = remindersItem

                        }
                }
            }
            .addOnCompleteListener {
                _isRefreshing.value = true
            }
    }


    // update isChecked to true when user click the button
    fun updateItem(documentID: String) {

        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // get click item
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(REMINDERS)
                        .whereEqualTo(DOCUMENTID, documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {

                                // update item to check status
                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(REMINDERS)
                                    .document(documentID)
                                    .update(ISCHECKED, true)
                            }
                        }
                }
            }
    }

    //update countdown item to overdue depends on time
    fun updateCountdown(documentId: String) {
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // get overdue countdowns
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(COUNTDOWN)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {

                                // update countdowns to overdue status
                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(COUNTDOWN)
                                    .document(documentId)
                                    .update(OVERDUE, true)
                            }
                        }
                }
            }
    }

    // delete item due to swipe specific item
    fun deleteItem(title: String) {

        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // delete reminders
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(REMINDERS)
                        .whereEqualTo(TITLE, title)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {

                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(REMINDERS)
                                    .document(reminders.id)
                                    .delete()

                                // delete calendar
                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .delete()
                            }
                        }
                }
            }
    }
}
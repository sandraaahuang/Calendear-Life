package com.sandra.calendearlife

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ISCHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETREMINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel : ViewModel() {

    val locale: Locale =
        if (Locale.getDefault().toString() == CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", locale)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

    var db = FirebaseFirestore.getInstance()

    // Record current fragment to support data binding
    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    val userEmail = UserManager.userEmail
    val userPhoto = UserManager.userPhoto

    lateinit var remindAdd: Reminders
    private val _liveReminders = MutableLiveData<Reminders>()
    val liveReminders: LiveData<Reminders>
        get() = _liveReminders

    private val dnrItem = ArrayList<Reminders>()
    private val _livednr = MutableLiveData<List<Reminders>>()
    val livednr: LiveData<List<Reminders>>
        get() = _livednr

    fun getItem(documentId: String) {
        //connect to countdown data ( only the item that overdue is false )
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    //get reminders ( only ischecked is false )
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(REMINDERS)
                        .whereEqualTo(DOCUMENTID, documentId)
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
                                _liveReminders.value = remindAdd
                            }
                        }
                }
            }
    }

    fun dnrItem() {
        //connect to countdown data ( only the item that overdue is false )
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

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

                                dnrItem.add(remindAdd)
                            }
                            _livednr.value = dnrItem
                        }
                }
            }
    }



    fun writeGoogleItem(item: Any, documentId: String) {

        // get all data from user at first
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .document(documentId)
            .set(item)
    }
}

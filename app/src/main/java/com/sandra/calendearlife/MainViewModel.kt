package com.sandra.calendearlife

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.sandra.calendearlife.constant.DATE_TIME_FORMAT
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SIMPLE_DATE_FORMAT
import com.sandra.calendearlife.constant.transferTimestamp2String
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.CurrentFragmentType
import com.sandra.calendearlife.util.UserManager
import java.util.*

class MainViewModel : ViewModel() {

    var db = FirebaseFirestore.getInstance()

    // Record current fragment to support data binding
    val currentFragmentType = MutableLiveData<CurrentFragmentType>()

    val userEmail = UserManager.userEmail
    val userPhoto = UserManager.userPhoto

    lateinit var remindAdd: Reminders
    private val _liveReminders = MutableLiveData<Reminders>()
    val liveReminders: LiveData<Reminders>
        get() = _liveReminders

    private val set4AlarmManagerReminderItem = ArrayList<Reminders>()
    private val _liveSet4AlarmManagerReminderItem = MutableLiveData<List<Reminders>>()
    val liveSet4AlarmManagerReminderItem: LiveData<List<Reminders>>
        get() = _liveSet4AlarmManagerReminderItem

    fun getItem(documentId: String) {

        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        //get reminders ( only ischecked is false )
                        db.collection(DATA)
                            .document(it)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(DOCUMENT_ID, documentId)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminder in remindersDocuments) {

                                    setupRemindersItem(reminder)

                                    _liveReminders.value = remindAdd
                                }
                            }
                    }
                }
        }

    }

    fun set4AlarmManagerReminderItem() {

        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        //get reminders ( only is_checked is false )
                        db.collection(DATA)
                            .document(it)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(IS_CHECKED, false)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminder in remindersDocuments) {

                                    setupRemindersItem(reminder)

                                    set4AlarmManagerReminderItem.add(remindAdd)
                                }
                                _liveSet4AlarmManagerReminderItem.value = set4AlarmManagerReminderItem
                            }
                    }
                }
        }

    }



    fun writeGoogleItem(item: Any, documentId: String) {

        // get all data from user at first
        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .document(documentId)
                .set(item)
        }

    }

    private fun setupRemindersItem(reminder: QueryDocumentSnapshot) {
        remindAdd = Reminders(
            transferTimestamp2String(SIMPLE_DATE_FORMAT,
                reminder.data[SET_DATE] as Timestamp),
            reminder.data[TITLE].toString(),
            reminder.data[HAS_REMIND_DATE].toString().toBoolean(),
            transferTimestamp2String(DATE_TIME_FORMAT,
                reminder.data[REMIND_DATE] as Timestamp),
            reminder.data[REMIND_DATE] as Timestamp,
            reminder.data[IS_CHECKED].toString().toBoolean(),
            reminder.data[NOTE].toString(),
            reminder.data[FREQUENCY].toString(),
            reminder.data[DOCUMENT_ID].toString()
        )
    }
}

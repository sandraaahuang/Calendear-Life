package com.sandra.calendearlife.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SIMPLE_DATE_FORMAT
import com.sandra.calendearlife.constant.transferTimestamp2String
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager

class HistoryRemindersViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private lateinit var remindAdd: Reminders

    private val remindersItem = ArrayList<Reminders>()
    private val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders: LiveData<List<Reminders>>
        get() = _liveReminders

    init {
        getItem()
    }

    private fun getItem() {
        //connect to countdown data ( only the item that overdue is false )
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        //get reminders ( only is_checked is true )
                        db.collection(DATA)
                            .document(userManagerId)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(IS_CHECKED, true)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminder in remindersDocuments) {

                                    remindAdd = Reminders(
                                        transferTimestamp2String(SIMPLE_DATE_FORMAT, reminder.data[SET_DATE] as Timestamp),
                                        reminder.data[TITLE].toString(),
                                        reminder.data[SET_REMIND_DATE].toString().toBoolean(),
                                        transferTimestamp2String(SIMPLE_DATE_FORMAT, reminder.data[REMIND_DATE] as Timestamp),
                                        reminder.data[REMIND_DATE] as Timestamp,
                                        reminder.data[IS_CHECKED].toString().toBoolean(),
                                        reminder.data[NOTE].toString(),
                                        reminder.data[FREQUENCY].toString(),
                                        reminder.data[DOCUMENT_ID].toString()
                                    )

                                    remindersItem.add(remindAdd)
                                }
                                _liveReminders.value = remindersItem

                            }
                    }
                }
        }
    }
}
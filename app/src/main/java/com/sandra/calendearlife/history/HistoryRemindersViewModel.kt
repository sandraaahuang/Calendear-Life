package com.sandra.calendearlife.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.Const
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
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryRemindersViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    val locale: Locale =
        if (Locale.getDefault().toString() == CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

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
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    //get reminders ( only ischecked is true )
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(REMINDERS)
                        .whereEqualTo(ISCHECKED, true)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminder in documents) {

                                val setDate = (reminder.data[SETDATE] as Timestamp)
                                val remindDate = (reminder.data[REMINDDATE] as Timestamp)

                                remindAdd = Reminders(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    reminder.data[TITLE].toString(),
                                    reminder.data[SETREMINDATE].toString().toBoolean(),
                                    simpleDateFormat.format(remindDate.seconds * 1000),
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
    }
}
package com.sandra.calendearlife.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.getRemindersItemFromFirebase
import com.sandra.calendearlife.util.UserManager

class HistoryRemindersViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private val remindersItem = ArrayList<Reminders>()
    private val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders: LiveData<List<Reminders>>
        get() = _liveReminders

    init {
        getItem()
    }

    private fun getItem() {
        //connect to countdown data ( only the item that isOverdue is false )
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

                                    getRemindersItemFromFirebase(reminder, remindersItem)

                                }
                                _liveReminders.value = remindersItem

                            }
                    }
                }
        }
    }
}
package com.sandra.calendearlife.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.sandra.calendearlife.getCountdownItemFromFirebase
import com.sandra.calendearlife.getRemindersItemFromFirebase
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager

class HomeViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private val countdownItem = ArrayList<Countdown>()
    private val _liveCountdown = MutableLiveData<List<Countdown>>()
    val liveCountdown: LiveData<List<Countdown>>
        get() = _liveCountdown

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
        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        getNotOverdueCountdown(calendar)
                        getNotCheckReminders(calendar)
                    }
                }
                .addOnCompleteListener {
                    _isRefreshing.value = true
                }
        }
    }

    private fun getNotOverdueCountdown(calendar: QueryDocumentSnapshot) {
        // get countdowns
        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .document(calendar.id)
                .collection(COUNTDOWN)
                .whereEqualTo(OVERDUE, false)
                .get()
                .addOnSuccessListener { countdownDocuments ->

                    for (countdown in countdownDocuments) {

                        getCountdownItemFromFirebase(countdown, countdownItem)

                    }

                    _liveCountdown.value = countdownItem

                }
        }
    }

    private fun getNotCheckReminders(calendar: QueryDocumentSnapshot) {
        //get reminders ( only is_checked is false )
        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .document(calendar.id)
                .collection(REMINDERS)
                .whereEqualTo(IS_CHECKED, false)
                .get()
                .addOnSuccessListener { documents ->

                    for (reminder in documents) {

                        getRemindersItemFromFirebase(reminder, remindersItem)

                    }
                    _liveReminders.value = remindersItem

                }
        }

    }

    // update isChecked to true when user click the button
    fun updateCheckedReminder(documentID: String) {

        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        // get click item
                        db.collection(DATA)
                            .document(it)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(DOCUMENT_ID, documentID)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminders in remindersDocuments) {

                                    // update item to check status
                                    db.collection(DATA)
                                        .document(it)
                                        .collection(CALENDAR)
                                        .document(calendar.id)
                                        .collection(REMINDERS)
                                        .document(documentID)
                                        .update(IS_CHECKED, true)
                                }
                            }
                    }
                }
        }

    }

    //update countdown item to overdue depends on time
    fun updateOverdueCountdown(documentId: String) {
        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        // get overdue countdowns
                        db.collection(DATA)
                            .document(it)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(COUNTDOWN)
                            .get()
                            .addOnSuccessListener { countdownDocuments ->

                                for (countdown in countdownDocuments) {

                                    // update countdowns to overdue status
                                    db.collection(DATA)
                                        .document(it)
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
    }

    // delete item due to swipe specific item
    fun deleteItem(title: String) {

        UserManager.id?.let {
            db.collection(DATA)
                .document(it)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for (calendar in documents) {

                        // delete reminders
                        db.collection(DATA)
                            .document(it)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(TITLE, title)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminders in remindersDocuments) {

                                    db.collection(DATA)
                                        .document(it)
                                        .collection(CALENDAR)
                                        .document(calendar.id)
                                        .collection(REMINDERS)
                                        .document(reminders.id)
                                        .delete()

                                    // delete calendar
                                    db.collection(DATA)
                                        .document(it)
                                        .collection(CALENDAR)
                                        .document(calendar.id)
                                        .delete()
                                }
                            }
                    }
                }
        }

    }
}
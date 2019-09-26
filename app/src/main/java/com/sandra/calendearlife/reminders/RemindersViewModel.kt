package com.sandra.calendearlife.reminders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.data.Reminders
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RemindersViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    val locale =
        if (Locale.getDefault().toString() == "zh-rtw") {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd", locale)
    val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd h:mm a", locale)
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    lateinit var remindAdd: Reminders

    val remindersItem = ArrayList<Reminders>()
    val _liveReminders = MutableLiveData<List<Reminders>>()
    val liveReminders: LiveData<List<Reminders>>
        get() = _liveReminders

    private var _updateCompleted = MutableLiveData<Boolean>()

    val updateCompleted: LiveData<Boolean>
        get() = _updateCompleted

    private var _clicked = MutableLiveData<Boolean>()

    val clicked: LiveData<Boolean>
        get() = _clicked

    init {
        getItem()
    }

    private val _navigateToReminderProperty = MutableLiveData<Reminders>()

    val navigateToReminderProperty: LiveData<Reminders>
        get() = _navigateToReminderProperty

    fun displayReminderDetails(reminders: Reminders) {
        _navigateToReminderProperty.value = reminders
    }

    fun displayReminderDetailsComplete() {
        _navigateToReminderProperty.value = null
    }

    fun writeItem(calendar: Any, reminder: Any) {
        _clicked.value = true
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .add(calendar)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "AddNewCalendar",
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .document(documentReference.id)
                    .update("documentID", documentReference.id)

                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .document(documentReference.id)
                    .collection("reminders")
                    .add(reminder)
                    .addOnSuccessListener { reminderID ->
                        Log.d(
                            "AddNewReminders",
                            "DocumentSnapshot added with ID: " + reminderID.id
                        )
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .document(documentReference.id)
                            .collection("reminders")
                            .document(reminderID.id)
                            .update("documentID", reminderID.id)
                    }
            }
            .addOnCompleteListener {
                _updateCompleted.value = true
            }
    }


    // also only get isChecked is false
    fun getItem() {
        //connect to countdown data
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // get reminders
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("isChecked", false)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminder in documents) {

                                val setDate =
                                    (reminder.data["setDate"] as com.google.firebase.Timestamp)
                                val remindDate =
                                    (reminder.data["remindDate"] as com.google.firebase.Timestamp)

                                remindAdd = Reminders(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    reminder.data["title"].toString(),
                                    reminder.data["setRemindDate"].toString().toBoolean(),
                                    dateTimeFormat.format(remindDate.seconds * 1000),
                                    reminder.data["remindDate"] as com.google.firebase.Timestamp,
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

    // update isChecked to true when user click the button
    fun updateItem(documentID: String) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // update reminders
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("documentID", documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {

                                // update reminders
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(documentID)
                                    .update("isChecked", true)
                            }
                        }
                }
            }
    }

    // delete item due to swipe specific item
    fun deleteItem(title: String) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // delete selected reminders
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("title", title)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {

                                // delete
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
                                    .delete()


                                // delete calendar
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .delete()
                            }
                        }
                }
            }
    }
}

package com.sandra.calendearlife.reminders

import android.app.Application
import android.view.View
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager

class RemindersDetailViewModel(reminders: Reminders, app: Application) : AndroidViewModel(app) {

    var db = FirebaseFirestore.getInstance()

    private val _selectedItem = MutableLiveData<Reminders>()
    val selectedItem: LiveData<Reminders>
        get() = _selectedItem

    private var _isUpdateCompleted = MutableLiveData<Boolean>()
    val isUpdateCompleted: LiveData<Boolean>
        get() = _isUpdateCompleted

    private var _isClicked = MutableLiveData<Boolean>()
    val isClicked: LiveData<Boolean>
        get() = _isClicked

    private var _showDatePicker = MutableLiveData<TextView>()
    val showDatePicker: LiveData<TextView>
        get() = _showDatePicker

    private var _showTimePicker = MutableLiveData<TextView>()
    val showTimePicker: LiveData<TextView>
        get() = _showTimePicker

    init {
        _selectedItem.value = reminders
    }

    //update item
    fun updateItem(item: HashMap<String, Any>, calendarItem: HashMap<String, Any>, documentID: String) {
        _isClicked.value = true

        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for ((index, calendar) in documents.withIndex()) {

                        // get update reminder item
                        db.collection(DATA)
                            .document(userManagerId)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(DOCUMENT_ID, documentID)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminders in remindersDocuments) {

                                    // update reminders
                                    db.collection(DATA)
                                        .document(userManagerId)
                                        .collection(CALENDAR)
                                        .document(calendar.id)
                                        .collection(REMINDERS)
                                        .document(reminders.id)
                                        .update(item)

                                    // update calendar
                                    db.collection(DATA)
                                        .document(userManagerId)
                                        .collection(CALENDAR)
                                        .document(calendar.id)
                                        .update(calendarItem)
                                }
                            }

                        if (index == documents.size() -1) {
                            _isUpdateCompleted.value = true
                        }
                    }
                }
        }
    }


    //delete item
    fun deleteItem(documentID: String) {
        _isClicked.value = true

        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .get()
                .addOnSuccessListener { documents ->

                    for ((index, calendar) in documents.withIndex()) {

                        // delete selected deleted item
                        db.collection(DATA)
                            .document(userManagerId)
                            .collection(CALENDAR)
                            .document(calendar.id)
                            .collection(REMINDERS)
                            .whereEqualTo(DOCUMENT_ID, documentID)
                            .get()
                            .addOnSuccessListener { remindersDocuments ->

                                for (reminders in remindersDocuments) {

                                    // delete reminders
                                    db.collection(DATA)
                                        .document(userManagerId)
                                        .collection(CALENDAR)
                                        .document(calendar.id)
                                        .collection(REMINDERS)
                                        .document(reminders.id)
                                        .delete()

                                    db.collection(DATA)
                                        .document(userManagerId)
                                        .collection(CALENDAR)
                                        .document(calendar.id)
                                        .delete()

                                }
                            }

                        if (index == documents.size() -1) {
                            _isUpdateCompleted.value = true
                        }
                    }
                }
        }
    }

    fun onButtonClick(view: View) {
        when (view.id) {
            R.id.remindDate -> showDatePicker(view.findViewById(R.id.remindDate))
            R.id.remindTime -> showTimePicker(view.findViewById(R.id.remindTime))
        }
    }

    private fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }

    private fun showTimePicker(clickText: TextView) {
        _showTimePicker.value = clickText
    }
}
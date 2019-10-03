package com.sandra.calendearlife.reminders

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.util.UserManager

class RemindersViewModel : ViewModel() {
    
    var db = FirebaseFirestore.getInstance()

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


    fun writeItem(calendar: Any, reminder: Any) {
        _isClicked.value = true
        
        UserManager.id?.let { userManagerId ->
            db.collection(DATA)
                .document(userManagerId)
                .collection(CALENDAR)
                .add(calendar)
                .addOnSuccessListener { documentReference ->

                    db.collection(DATA)
                        .document(userManagerId)
                        .collection(CALENDAR)
                        .document(documentReference.id)
                        .update(DOCUMENT_ID, documentReference.id)

                    db.collection(DATA)
                        .document(userManagerId)
                        .collection(CALENDAR)
                        .document(documentReference.id)
                        .collection(REMINDERS)
                        .add(reminder)
                        .addOnSuccessListener { reminderID ->

                            db.collection(DATA)
                                .document(userManagerId)
                                .collection(CALENDAR)
                                .document(documentReference.id)
                                .collection(REMINDERS)
                                .document(reminderID.id)
                                .update(DOCUMENT_ID, reminderID.id)
                        }
                }
                .addOnCompleteListener {
                    _isUpdateCompleted.value = true
                }
        }
    }

    fun onButtonClick(view: View) {
        when (view.id) {
            R.id.remindersDateInput -> showDatePicker(view.findViewById(R.id.remindersDateInput))
            R.id.remindersTimeInput -> showTimePicker(view.findViewById(R.id.remindersTimeInput))
        }
    }

    private fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }

    private fun showTimePicker(clickText: TextView) {
        _showTimePicker.value = clickText
    }
}

package com.sandra.calendearlife.reminders

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.Const
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*

class RemindersViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private val locale =
        if (Locale.getDefault().toString() == CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd", locale)

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
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .add(calendar)
            .addOnSuccessListener { documentReference ->

                db.collection(DATA)
                    .document(UserManager.id!!)
                    .collection(CALENDAR)
                    .document(documentReference.id)
                    .update(DOCUMENTID, documentReference.id)

                db.collection(DATA)
                    .document(UserManager.id!!)
                    .collection(CALENDAR)
                    .document(documentReference.id)
                    .collection(REMINDERS)
                    .add(reminder)
                    .addOnSuccessListener { reminderID ->

                        db.collection(DATA)
                            .document(UserManager.id!!)
                            .collection(CALENDAR)
                            .document(documentReference.id)
                            .collection(REMINDERS)
                            .document(reminderID.id)
                            .update(DOCUMENTID, reminderID.id)
                    }
            }
            .addOnCompleteListener {
                _isUpdateCompleted.value = true
            }
    }

    fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }

    fun showTimePicker(clickText: TextView) {
        _showTimePicker.value = clickText
    }
}

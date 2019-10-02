package com.sandra.calendearlife.countdown

import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.util.UserManager

class CountdownViewModel : ViewModel() {
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

    fun writeItem(calendar: Any, countdown: Any) {
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
                    .collection(COUNTDOWN)
                    .add(countdown)
                    .addOnSuccessListener { countdownID ->

                        db.collection(DATA)
                            .document(UserManager.id!!)
                            .collection(CALENDAR)
                            .document(documentReference.id)
                            .collection(COUNTDOWN)
                            .document(countdownID.id)
                            .update(DOCUMENTID, countdownID.id)
                    }
            }
            .addOnCompleteListener {
                _isUpdateCompleted.value = true
            }
    }

    fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }
}

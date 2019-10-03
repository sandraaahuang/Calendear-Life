package com.sandra.calendearlife.countdown

import android.view.View
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENT_ID
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
                        .collection(COUNTDOWN)
                        .add(countdown)
                        .addOnSuccessListener { countdownID ->

                            db.collection(DATA)
                                .document(userManagerId)
                                .collection(CALENDAR)
                                .document(documentReference.id)
                                .collection(COUNTDOWN)
                                .document(countdownID.id)
                                .update(DOCUMENT_ID, countdownID.id)
                        }
                }
                .addOnCompleteListener {
                    _isUpdateCompleted.value = true
                }
        }
    }

    fun onButtonClick(view: View) {
        when (view.id) {
            R.id.countdownDateInput -> showDatePicker(view.findViewById(R.id.countdownDateInput))
        }
    }

    private fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }
}

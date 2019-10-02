package com.sandra.calendearlife.countdown

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*

class CountdownViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    private var _isUpdateCompleted = MutableLiveData<Boolean>()

    val isUpdateCompleted: LiveData<Boolean>
        get() = _isUpdateCompleted

    private var _isClicked = MutableLiveData<Boolean>()

    val isClicked: LiveData<Boolean>
        get() = _isClicked

    fun writeItem(calendar: Any, countdown: Any) {
        _isClicked.value = true
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
                    .collection("countdowns")
                    .add(countdown)
                    .addOnSuccessListener { countdownID ->
                        Log.d(
                            "AddNewCountdowns",
                            "DocumentSnapshot added with ID: " + countdownID.id
                        )
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .document(documentReference.id)
                            .collection("countdowns")
                            .document(countdownID.id)
                            .update("documentID", countdownID.id)
                    }
            }
            .addOnCompleteListener {
                _isUpdateCompleted.value = true
            }
    }
}

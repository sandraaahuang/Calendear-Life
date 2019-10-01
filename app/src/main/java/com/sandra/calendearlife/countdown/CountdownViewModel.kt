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

    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    private var _updateCompleted = MutableLiveData<Boolean>()

    val updateCompleted: LiveData<Boolean>
        get() = _updateCompleted

    private var _clicked = MutableLiveData<Boolean>()

    val clicked: LiveData<Boolean>
        get() = _clicked

    fun writeItem(calendar: Any, countdown: Any) {
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
                _updateCompleted.value = true
            }
    }
}

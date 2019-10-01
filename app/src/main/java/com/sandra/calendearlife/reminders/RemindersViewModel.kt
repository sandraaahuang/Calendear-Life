package com.sandra.calendearlife.reminders

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager
import java.text.SimpleDateFormat
import java.util.*

class RemindersViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    val locale =
        if (Locale.getDefault().toString() == "zh-rtw") {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd", locale)
    val date = Date(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH)

    private var _isUpdateCompleted = MutableLiveData<Boolean>()

    val isUpdateCompleted: LiveData<Boolean>
        get() = _isUpdateCompleted

    private var _isClicked = MutableLiveData<Boolean>()

    val isClicked: LiveData<Boolean>
        get() = _isClicked


    fun writeItem(calendar: Any, reminder: Any) {
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
                _isUpdateCompleted.value = true
            }
    }
}

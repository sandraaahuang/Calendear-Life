package com.sandra.calendearlife.countdown

import android.app.Application
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.constant.FirebaseKey.Companion.CALENDAR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DOCUMENTID
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.util.UserManager

class CountdownDetailViewModel(countdown: Countdown, app: Application) : AndroidViewModel(app) {

    var db = FirebaseFirestore.getInstance()

    private val _selectedItem = MutableLiveData<Countdown>()
    val selectedItem: LiveData<Countdown>
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

    init {
        _selectedItem.value = countdown
    }


    //update item
    fun updateItem(item: HashMap<String, Any>, calendarItem: HashMap<String, Any>, documentID: String) {
        _isClicked.value = true
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for ((index, calendar) in documents.withIndex()) {

                    // get update item
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(COUNTDOWN)
                        .whereEqualTo(DOCUMENTID, documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {

                                // update countdowns
                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(COUNTDOWN)
                                    .document(countdown.id)
                                    .update(item)

                                // update calendar
                                db.collection(DATA)
                                    .document(UserManager.id!!)
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


    //delete item
    fun deleteItem(documentID: String) {
        _isClicked.value = true
        db.collection(DATA)
            .document(UserManager.id!!)
            .collection(CALENDAR)
            .get()
            .addOnSuccessListener { documents ->

                for ((index, calendar) in documents.withIndex()) {

                    // get delete item
                    db.collection(DATA)
                        .document(UserManager.id!!)
                        .collection(CALENDAR)
                        .document(calendar.id)
                        .collection(COUNTDOWN)
                        .whereEqualTo(DOCUMENTID, documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {

                                // delete countdown
                                db.collection(DATA)
                                    .document(UserManager.id!!)
                                    .collection(CALENDAR)
                                    .document(calendar.id)
                                    .collection(COUNTDOWN)
                                    .document(countdown.id)
                                    .delete()

                                // delete calendar
                                db.collection(DATA)
                                    .document(UserManager.id!!)
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

    fun showDatePicker(clickText: TextView) {
        _showDatePicker.value = clickText
    }
}


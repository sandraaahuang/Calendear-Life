package com.sandra.calendearlife.countdown

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.data.Countdown

class CountdownDetailViewModel(countdown: Countdown, app: Application) : AndroidViewModel(app) {

    var db = FirebaseFirestore.getInstance()

    private val _selectedItem = MutableLiveData<Countdown>()

    val selectedItem: LiveData<Countdown>
        get() = _selectedItem

    init {
        _selectedItem.value = countdown
    }


    //update item
    fun updateItem(item: HashMap<String, Any>, calendarItem: HashMap<String, Any>, documentID: String) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // get update item
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .whereEqualTo("documentID", documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {

                                // update countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdown.id)
                                    .update(item)

                                // update calendar
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .update(calendarItem)
                            }
                        }
                }
            }
    }


    //delete item
    fun deleteItem(documentID: String) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // get delete item
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .whereEqualTo("documentID", documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {

                                // delete countdown
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdown.id)
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


package com.sandra.calendearlife.reminders

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.data.Reminders

class RemindersDetailViewModel(reminders: Reminders, app: Application) : AndroidViewModel(app) {

    var db = FirebaseFirestore.getInstance()

    private val _selectedItem = MutableLiveData<Reminders>()

    val selectedItem: LiveData<Reminders>
        get() = _selectedItem

    init {
        _selectedItem.value = reminders
    }

    //update item
    fun updateItem(item: HashMap<String, Any>, calendarItem: HashMap<String, Any>, documentID: String) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {

                    // get update reminder item
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("documentID", documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {

                                // update reminders
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
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

                    // delete selected deleted item
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("documentID", documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminders in documents) {

                                // delete reminders
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("reminders")
                                    .document(reminders.id)
                                    .delete()

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
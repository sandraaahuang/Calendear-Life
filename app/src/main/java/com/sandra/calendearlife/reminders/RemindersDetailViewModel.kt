package com.sandra.calendearlife.reminders

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Reminders

class RemindersDetailViewModel(reminders: Reminders,app: Application) : AndroidViewModel(app) {

    var db = FirebaseFirestore.getInstance()

    private val _selectedItem = MutableLiveData<Reminders>()

    val selectedItem: LiveData<Reminders>
        get() = _selectedItem

    init {
        _selectedItem.value = reminders
    }

    //update item
    fun updateItem(item: HashMap<String, Any>,documentID: String){

        db.collection("data")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (data in task.result!!) {
                        Log.d("getAllDate", data.id + " => " + data.data)

                        db.collection("data")
                            .document(data.id)
                            .collection("calendar")
                            .get()
                            .addOnSuccessListener { documents ->

                                for (calendar in documents) {
                                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                                    // add countdowns
                                    db.collection("data")
                                        .document(data.id)
                                        .collection("calendar")
                                        .document(calendar.id)
                                        .collection("reminders")
                                        .whereEqualTo("documentID", documentID)
                                        .get()
                                        .addOnSuccessListener { documents ->

                                            for (reminders in documents) {
                                                Log.d("getAllCalendar", "${reminders.id} => ${reminders.data}")

                                                // add countdowns
                                                db.collection("data")
                                                    .document(data.id)
                                                    .collection("calendar")
                                                    .document(calendar.id)
                                                    .collection("reminders")
                                                    .document(reminders.id)
                                                    .update(item)
                                                    .addOnSuccessListener { Log.d("RenewCountdown", "successfully updated my status!") }
                                                    .addOnFailureListener { e -> Log.w("RenewCountdown", "Error updating document", e) }
                                            }
                                        }

                                        .addOnFailureListener { exception ->
                                            Log.w("getAllCalendar", "Error getting documents: ", exception)
                                        }
                                }
                            }

                            .addOnFailureListener { exception ->
                                Log.w("getAllCalendar", "Error getting documents: ", exception)
                            }

                    }
                } else {
                    Log.w("getAllDate", "Error getting documents.", task.exception)
                }
            }

    }

    //delete item
    fun deleteItem(documentID: String){

        db.collection("data")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (data in task.result!!) {
                        Log.d("getAllDate", data.id + " => " + data.data)

                        db.collection("data")
                            .document(data.id)
                            .collection("calendar")
                            .get()
                            .addOnSuccessListener { documents ->

                                for (calendar in documents) {
                                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                                    // add countdowns
                                    db.collection("data")
                                        .document(data.id)
                                        .collection("calendar")
                                        .document(calendar.id)
                                        .collection("reminders")
                                        .whereEqualTo("documentID", documentID)
                                        .get()
                                        .addOnSuccessListener { documents ->

                                            for (reminders in documents) {
                                                Log.d("getAllCalendar", "${reminders.id} => ${reminders.data}")

                                                // add countdowns
                                                db.collection("data")
                                                    .document(data.id)
                                                    .collection("calendar")
                                                    .document(calendar.id)
                                                    .collection("reminders")
                                                    .document(reminders.id)
                                                    .delete()
                                                    .addOnSuccessListener { Log.d("RenewCountdown", "id = $documentID") }
                                                    .addOnFailureListener { e -> Log.w("RenewCountdown", "Error updating document", e) }
                                            }
                                        }

                                        .addOnFailureListener { exception ->
                                            Log.w("getAllCalendar", "Error getting documents: ", exception)
                                        }
                                }
                            }

                            .addOnFailureListener { exception ->
                                Log.w("getAllCalendar", "Error getting documents: ", exception)
                            }

                    }
                } else {
                    Log.w("getAllDate", "Error getting documents.", task.exception)
                }
            }

    }



}
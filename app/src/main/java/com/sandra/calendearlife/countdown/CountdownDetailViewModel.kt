package com.sandra.calendearlife.countdown

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.UserManager
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
    fun updateItem(item: HashMap<String, Any>, documentID: String) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // add countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .whereEqualTo("documentID", documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {
                                Log.d("getAllCalendar", "${countdown.id} => ${countdown.data}")

                                // add countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdown.id)
                                    .update(item)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "RenewCountdown",
                                            "successfully updated my status!"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "RenewCountdown",
                                            "Error updating document",
                                            e
                                        )
                                    }
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


    //delete item
    fun deleteItem(documentID: String) {

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // add countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .whereEqualTo("documentID", documentID)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {
                                Log.d("getAllCalendar", "${countdown.id} => ${countdown.data}")

                                // add countdowns
                                db.collection("data")
                                    .document(UserManager.id!!)
                                    .collection("calendar")
                                    .document(calendar.id)
                                    .collection("countdowns")
                                    .document(countdown.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "RenewCountdown",
                                            "id = $documentID"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "RenewCountdown",
                                            "Error updating document",
                                            e
                                        )
                                    }
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
}


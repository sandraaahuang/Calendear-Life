package com.sandra.calendearlife.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class CalendarEventViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    fun writeItem(item: Any, countdown: Any, reminder: Any) {

        // get all data
        db.collection("data")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (data in task.result!!) {
                        Log.d("getAllDate", data.id + " => " + data.data)

                        // add calendar
                        db.collection("data")
                            .document(data.id)
                            .collection("calendar")
                            .add(item)
                            .addOnSuccessListener { documentReference ->
                                Log.d(
                                    "AddCountdownsIntoDB",
                                    "DocumentSnapshot added with ID: " + documentReference.id
                                )

                                // update calendar document id
                                db.collection("data")
                                    .document(data.id)
                                    .collection("calendar")
                                    .document(documentReference.id)
                                    .update("documentID", documentReference.id)
                                    .addOnSuccessListener {
                                        // add reminders
                                        db.collection("data")
                                            .document(data.id)
                                            .collection("calendar")
                                            .whereEqualTo("hasReminders", true)
                                            .get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    for (document in task.result!!) {
                                                        Log.d("All calendar", document.id + " => " + document.data)
                                                        // add reminders
                                                        db.collection("data")
                                                            .document(data.id)
                                                            .collection("calendar")
                                                            .document(documentReference.id)
                                                            .collection("reminders")
                                                            .add(reminder)
                                                            .addOnSuccessListener { documentReference ->
                                                                Log.d(
                                                                    "AddCountdownsIntoDB",
                                                                    "DocumentSnapshot added with ID: " + documentReference.id
                                                                )
                                                                db.collection("data")
                                                                    .document(data.id)
                                                                    .collection("calendar")
                                                                    .document(document.id)
                                                                    .collection("reminders")
                                                                    .document(documentReference.id)
                                                                    .update("documentID", documentReference.id)


                                                            }
                                                    }
                                                }
                                            }
                                        // add countdown
                                        db.collection("data")
                                            .document(data.id)
                                            .collection("calendar")
                                            .whereEqualTo("hasCountdown", true)
                                            .get()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    for (document in task.result!!) {
                                                        Log.d("All calendar", document.id + " => " + document.data)
                                                        // add reminders
                                                        db.collection("data")
                                                            .document(data.id)
                                                            .collection("calendar")
                                                            .document(documentReference.id)
                                                            .collection("countdowns")
                                                            .add(countdown)
                                                            .addOnSuccessListener { documentReference ->
                                                                Log.d(
                                                                    "AddCountdownsIntoDB",
                                                                    "DocumentSnapshot added with ID: " + documentReference.id
                                                                )
                                                                db.collection("data")
                                                                    .document(data.id)
                                                                    .collection("calendar")
                                                                    .document(document.id)
                                                                    .collection("countdowns")
                                                                    .document(documentReference.id)
                                                                    .update("documentID", documentReference.id)


                                                            }
                                                    }
                                                }
                                            }
                                    }
                            }
                    }
                }
            }
    }
}
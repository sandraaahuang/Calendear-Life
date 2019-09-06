package com.sandra.calendearlife.calendar.event

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.UserManager

class CalendarEventViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    fun writeItem(item: Any, countdown: Any, reminder: Any) {

        // get all data from user at first
        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .add(item)
            .addOnSuccessListener { CdocumentReference ->
                Log.d(
                    "AddCountdownsIntoDB",
                    "DocumentSnapshot added with ID: " + CdocumentReference.id
                )

                // update calendar document id and color ( pure calendar first)
                db.collection("data")
                    .document(UserManager.id!!)
                    .collection("calendar")
                    .document(CdocumentReference.id)
                    .update("documentID", CdocumentReference.id, "color", "af8eb5")
                    .addOnSuccessListener {
                        // add reminders
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .whereEqualTo("hasReminders", true)
                            .whereEqualTo("documentID", CdocumentReference.id)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    for (document in task.result!!) {
                                        Log.d("All calendar", document.id + " => " + document.data)
                                        // add reminders
                                        db.collection("data")
                                            .document(UserManager.id!!)
                                            .collection("calendar")
                                            .document(CdocumentReference.id)
                                            .collection("reminders")
                                            .add(reminder)
                                            .addOnSuccessListener { documentReference ->
                                                Log.d(
                                                    "AddCountdownsIntoDB",
                                                    "DocumentSnapshot added with ID: " + documentReference.id
                                                )
                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .document(document.id)
                                                    .collection("reminders")
                                                    .document(documentReference.id)
                                                    .update("documentID", documentReference.id)

                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .document(document.id)
                                                    .update("color", "81b9bf")
                                            }
                                    }
                                }
                            }
                        // add countdown
                        db.collection("data")
                            .document(UserManager.id!!)
                            .collection("calendar")
                            .whereEqualTo("hasCountdown", true)
                            .whereEqualTo("documentID", CdocumentReference.id)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    for (document in task.result!!) {
                                        Log.d("All calendar", document.id + " => " + document.data)
                                        // add reminders
                                        db.collection("data")
                                            .document(UserManager.id!!)
                                            .collection("calendar")
                                            .document(CdocumentReference.id)
                                            .collection("countdowns")
                                            .add(countdown)
                                            .addOnSuccessListener { documentReference ->
                                                Log.d(
                                                    "AddCountdownsIntoDB",
                                                    "DocumentSnapshot added with ID: " + documentReference.id
                                                )
                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .document(document.id)
                                                    .collection("countdowns")
                                                    .document(documentReference.id)
                                                    .update("documentID", documentReference.id)


                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .document(document.id)
                                                    .update("color", "cb9b8c")


                                                // item that have both reminders and countdown

                                                db.collection("data")
                                                    .document(UserManager.id!!)
                                                    .collection("calendar")
                                                    .whereEqualTo("hasCountdown", true)
                                                    .whereEqualTo("hasReminders", true)
                                                    .whereEqualTo("documentID", CdocumentReference.id)
                                                    .get()
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            for (document in task.result!!) {
                                                                Log.d("All calendar", document.id + " => " + document.data)
                                                                //update color

                                                                db.collection("data")
                                                                    .document(UserManager.id!!)
                                                                    .collection("calendar")
                                                                    .document(document.id)
                                                                    .update("color", "a69b97")


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
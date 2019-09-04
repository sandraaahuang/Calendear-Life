package com.sandra.calendearlife.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.data.Reminders
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalendarEventViewModel : ViewModel() {
    var db = FirebaseFirestore.getInstance()

    fun writeItem(item: Any) {

        db.collection("data")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (data in task.result!!) {
                        Log.d("getAllDate", data.id + " => " + data.data)

                        db.collection("data")
                            .document(data.id)
                            .collection("calendar")
                            .add(item)
                            .addOnSuccessListener { documentReference ->
                                Log.d(
                                    "AddCountdownsIntoDB",
                                    "DocumentSnapshot added with ID: " + documentReference.id
                                )
                                db.collection("data")
                                    .document(data.id)
                                    .collection("calendar")
                                    .document(documentReference.id)
                                    .update("documentID", documentReference.id)
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

    //write event into reminders

    fun writeReminders(item: Any){

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
                                        .add(item)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d(
                                                "AddCountdownsIntoDB",
                                                "DocumentSnapshot added with ID: " + documentReference.id
                                            )
                                            db.collection("data")
                                                .document(data.id)
                                                .collection("calendar")
                                                .document(calendar.id)
                                                .collection("reminders")
                                                .document(documentReference.id)
                                                .update("documentID", documentReference.id)


                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("AddCountdownsIntoDB", "Error adding document", e)
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

    // write countdown

    fun writeCountdown(item: Any){

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
                                        .collection("countdowns")
                                        .add(item)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d(
                                                "AddcountdownsIntoDB",
                                                "DocumentSnapshot added with ID: " + documentReference.id
                                            )
                                            db.collection("data")
                                                .document(data.id)
                                                .collection("calendar")
                                                .document(calendar.id)
                                                .collection("countdowns")
                                                .document(documentReference.id)
                                                .update("documentID", documentReference.id)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("AddcountdownsIntoDB", "Error adding document", e)
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
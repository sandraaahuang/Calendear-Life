package com.sandra.calendearlife.calendar.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.util.UserManager
import com.sandra.calendearlife.util.getString
import java.text.SimpleDateFormat

object Notification {

    fun countdownNotify() {

        val db = FirebaseFirestore.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

        lateinit var countdownAdd: Countdown
        val countdownItem = ArrayList<Countdown>()

        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    // get countdowns
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("countdowns")
                        .whereEqualTo("overdue", false)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (countdown in documents) {
                                Log.d("getAllcountdown", "${countdown.id} => ${countdown.data}")
                                val setDate = (countdown.data["setDate"] as Timestamp)
                                val targetDate = (countdown.data["targetDate"] as Timestamp)

                                countdownAdd = Countdown(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    countdown.data["title"].toString(),
                                    countdown.data["note"].toString(),
                                    simpleDateFormat.format(targetDate.seconds * 1000),
                                    countdown.data["targetDate"] as Timestamp,
                                    countdown.data["overdue"].toString().toBoolean(),
                                    countdown.data["documentID"].toString()
                                )

                                countdownItem.add(countdownAdd)
                            }

                            Log.d("sandraaacountdownItem", "countdownItem = $countdownItem")

                            for ((index, value) in countdownItem.withIndex()){

                                val textTitle = "${((value.targetTimestamp.seconds - Timestamp.now().seconds)/86400)} days " +
                                        "before ${value.title}"
                                val CHANNEL_ID = "Calendear"
                                val notificationId = index


                                val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.icon_has_google)
                                    .setContentTitle(textTitle)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setAutoCancel(true)

                                // Create the NotificationChannel, but only on API 26+ because
                                // the NotificationChannel class is new and not in the support library

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val name = getString(R.string.create_channel)
                                    val descriptionText =
                                        getString(R.string.create_channel)
                                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                                    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                                        description = descriptionText
                                    }
                                    // Register the channel with the system
                                    val notificationManager: NotificationManager = MyApplication.instance.
                                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                    notificationManager.createNotificationChannel(channel)
                                }
                                with(NotificationManagerCompat.from(MyApplication.instance)) {
                                    // notificationId is a unique int for each notification that you must define
                                    notify(notificationId, builder.build())
                                }
                            }

                        }

                }
            }
    }


    fun reminderNotify() {

        val db = FirebaseFirestore.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

        lateinit var remindAdd: Reminders
        val remindersItem = ArrayList<Reminders>()


        db.collection("data")
            .document(UserManager.id!!)
            .collection("calendar")
            .get()
            .addOnSuccessListener { documents ->

                for (calendar in documents) {
                    Log.d("getAllCalendar", "${calendar.id} => ${calendar.data}")

                    //get reminders ( only ischecked is false )
                    db.collection("data")
                        .document(UserManager.id!!)
                        .collection("calendar")
                        .document(calendar.id)
                        .collection("reminders")
                        .whereEqualTo("isChecked", false)
                        .whereEqualTo("setRemindDate", true)
                        .get()
                        .addOnSuccessListener { documents ->

                            for (reminder in documents) {
                                Log.d("getAllreminders", "${reminder.id} => ${reminder.data}")

                                val setDate = (reminder.data["setDate"] as Timestamp)
                                val remindDate = (reminder.data["remindDate"] as Timestamp)

                                remindAdd = Reminders(
                                    simpleDateFormat.format(setDate.seconds * 1000),
                                    reminder.data["title"].toString(),
                                    reminder.data["setRemindDate"].toString().toBoolean(),
                                    simpleDateFormat.format(remindDate.seconds * 1000),
                                    reminder.data["remindDate"] as Timestamp,
                                    reminder.data["isChecked"].toString().toBoolean(),
                                    reminder.data["note"].toString(),
                                    reminder.data["frequency"].toString(),
                                    reminder.data["documentID"].toString()
                                )

                                remindersItem.add(remindAdd)
                            }

                            Log.d("sandraaaremindersItem", "remindersItem = $remindersItem")

                            for ((index, value) in remindersItem.withIndex()){

                                val textTitle = value.title
                                val textContent = value.note
                                val CHANNEL_ID = "Calendear"
                                val notificationId = index


                                val builder = NotificationCompat.Builder(MyApplication.instance, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.icon_has_google)
                                    .setContentTitle(textTitle)
                                    .setContentText(textContent)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setAutoCancel(true)

                                // Create the NotificationChannel, but only on API 26+ because
                                // the NotificationChannel class is new and not in the support library

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val name = getString(R.string.create_channel)
                                    val descriptionText =
                                        getString(R.string.create_channel)
                                    val importance = NotificationManager.IMPORTANCE_DEFAULT
                                    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                                        description = descriptionText
                                    }
                                    // Register the channel with the system
                                    val notificationManager: NotificationManager = MyApplication.instance.
                                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                                    notificationManager.createNotificationChannel(channel)
                                }
                                with(NotificationManagerCompat.from(MyApplication.instance)) {
                                    // notificationId is a unique int for each notification that you must define
                                    notify(notificationId, builder.build())
                                }
                            }

                        }

                }
            }
    }

}
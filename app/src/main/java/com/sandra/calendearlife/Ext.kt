package com.sandra.calendearlife

import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.sandra.calendearlife.constant.DATE_TIME_FORMAT
import com.sandra.calendearlife.constant.FirebaseKey
import com.sandra.calendearlife.constant.SIMPLE_DATE_FORMAT
import com.sandra.calendearlife.constant.transferTimestamp2String
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders

fun getCalendarItemFromFirebase(calendar: QueryDocumentSnapshot, calendarItem: ArrayList<Calendar>) {
    calendarItem.add(Calendar(
        calendar.data[FirebaseKey.COLOR].toString(),
        transferTimestamp2String(SIMPLE_DATE_FORMAT, calendar.data[FirebaseKey.DATE] as Timestamp),
        transferTimestamp2String(SIMPLE_DATE_FORMAT, calendar.data[FirebaseKey.SET_DATE] as Timestamp),
        transferTimestamp2String(DATE_TIME_FORMAT, calendar.data[FirebaseKey.BEGIN_DATE] as Timestamp),
        transferTimestamp2String(DATE_TIME_FORMAT, calendar.data[FirebaseKey.END_DATE] as Timestamp),
        calendar.data[FirebaseKey.TITLE].toString(),
        calendar.data[FirebaseKey.NOTE].toString(),
        calendar.data[FirebaseKey.IS_ALL_DAY].toString().toBoolean(),
        calendar.data[FirebaseKey.HAS_LOCATION].toString().toBoolean(),
        calendar.data[FirebaseKey.LOCATION].toString(),
        calendar.data[FirebaseKey.HAS_REMINDERS].toString().toBoolean(),
        calendar.data[FirebaseKey.HAS_COUNTDOWN].toString().toBoolean(),
        calendar.data[FirebaseKey.DOCUMENT_ID].toString(),
        calendar.data[FirebaseKey.FREQUENCY].toString(),
        calendar.data[FirebaseKey.FROM_GOOGLE].toString().toBoolean(),
        transferTimestamp2String(DATE_TIME_FORMAT, calendar.data[FirebaseKey.REMINDERS_DATE] as Timestamp)
    ))
}

fun getCountdownItemFromFirebase(countdown: QueryDocumentSnapshot, countdownItem: ArrayList<Countdown>) {
    countdownItem.add(Countdown(
        transferTimestamp2String(SIMPLE_DATE_FORMAT, countdown.data[FirebaseKey.SET_DATE] as Timestamp),
        countdown.data[FirebaseKey.TITLE].toString(),
        countdown.data[FirebaseKey.NOTE].toString(),
        transferTimestamp2String(SIMPLE_DATE_FORMAT, countdown.data[FirebaseKey.TARGET_DATE] as Timestamp),
        countdown.data[FirebaseKey.TARGET_DATE] as Timestamp,
        countdown.data[FirebaseKey.IS_OVERDUE].toString().toBoolean(),
        countdown.data[FirebaseKey.DOCUMENT_ID].toString()
    ))
}

fun getRemindersItemFromFirebase(reminder: QueryDocumentSnapshot, remindersItem: ArrayList<Reminders>) {
    remindersItem.add(
        Reminders(
            transferTimestamp2String(SIMPLE_DATE_FORMAT, reminder.data[FirebaseKey.SET_DATE] as Timestamp),
            reminder.data[FirebaseKey.TITLE].toString(),
            reminder.data[FirebaseKey.HAS_REMIND_DATE].toString().toBoolean(),
            transferTimestamp2String(DATE_TIME_FORMAT, reminder.data[FirebaseKey.REMIND_DATE] as Timestamp),
            reminder.data[FirebaseKey.REMIND_DATE] as Timestamp,
            reminder.data[FirebaseKey.IS_CHECKED].toString().toBoolean(),
            reminder.data[FirebaseKey.NOTE].toString(),
            reminder.data[FirebaseKey.FREQUENCY].toString(),
            reminder.data[FirebaseKey.DOCUMENT_ID].toString()
        )
    )
}
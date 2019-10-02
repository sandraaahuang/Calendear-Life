package com.sandra.calendearlife.constant

import com.google.firebase.Timestamp

class FirebaseKey {
    companion object {
        // Collection path
        const val DATA = "data"
        const val CALENDAR = "calendar"
        const val COUNTDOWN = "countdowns"
        const val REMINDERS = "reminders"

        // Calendar column
        const val COLOR = "color"
        const val DATE = "date"
        const val SETDATE = "setDate"
        const val BEGINDATE = "beginDate"
        const val ENDDATE = "endDate"
        const val TITLE = "title"
        const val NOTE = "note"
        const val ISALLDAY = "isAllDay"
        const val HASLOCATION = "hasLocation"
        const val LOCATION = "location"
        const val HASREMINDERS = "hasReminders"
        const val HASCOUNTDOWN = "hasCountdown"
        const val DOCUMENTID = "documentID"
        const val FREQUENCY = "frequency"
        const val FROMGOOGLE = "fromGoogle"

        // Reminders column
        const val SETREMINDATE = "setRemindDate"
        const val REMINDDATE = "remindDate"
        const val REMINDTIMESTAMP = "remindTimestamp"
        const val ISCHECKED = "isChecked"

        // Countdown column
        const val TARGETDATE = "targetDate"
        const val TARGETTIMESTAMP = "targetTimestamp"
        const val OVERDUE = "overdue"

        // Color
        const val COLOR_REMIND = "C02942"
        const val COLOR_COUNTDOWN = "100038"
        const val COLOR_CAL = "8C6B8B"
        const val COLOR_REMIND_CAL = "542437"
        const val COLOR_COUNTDOWN_CAL = "cb9b8c"
        const val COLOR_ALL = "A6292F"
        const val COLOR_GOOGLE = "245E2C"
        const val COLOR_REMIND_GOOGLE = "542437"

    }
}
package com.sandra.calendearlife.constant

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
        const val SET_DATE = "setDate"
        const val BEGIN_DATE = "beginDate"
        const val END_DATE = "endDate"
        const val TITLE = "title"
        const val NOTE = "note"
        const val IS_ALL_DAY = "isAllDay"
        const val HAS_LOCATION = "hasLocation"
        const val LOCATION = "location"
        const val HAS_REMINDERS = "hasReminders"
        const val HAS_COUNTDOWN = "hasCountdown"
        const val DOCUMENT_ID = "documentID"
        const val FREQUENCY = "frequency"
        const val FROM_GOOGLE = "fromGoogle"
        const val REMINDERS_DATE = "remindersDate"

        // Reminders column
        const val HAS_REMIND_DATE = "hasRemindDate"
        const val REMIND_DATE = "remindDate"
        const val IS_CHECKED = "isChecked"

        // Countdown column
        const val TARGET_DATE = "targetDate"
        const val IS_OVERDUE = "isOverdue"

        // Color
        const val COLOR_REMIND = "C02942"
        const val COLOR_COUNTDOWN = "100038"
        const val COLOR_CAL = "8C6B8B"
        const val COLOR_REMIND_CAL = "542437"
        const val COLOR_COUNTDOWN_CAL = "cb9b8c"
        const val COLOR_ALL = "A6292F"
        const val COLOR_GOOGLE = "245E2C"

        // Google Calendar Constant
        const val PARENTHESES = "(("
        const val CONJUNCTION = " = ?) AND ("
        const val QUESTION_MARK = " = ?))"
        const val MAIL_FORMAT = "com.google"

        // User information
        const val USER_ID = "userId"
        const val USER_NAME = "userName"
        const val GMAIL = "gmail"
        const val HAS_ACCOUNT = "hasAccount"

    }
}
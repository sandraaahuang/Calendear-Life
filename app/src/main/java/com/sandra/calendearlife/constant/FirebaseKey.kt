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
        const val COLOR_REMIND = "C3AEC5"
        const val COLOR_COUNTDOWN = "DFB8C1"
        const val COLOR_CAL = "B2C18E"
        const val COLOR_REMIND_CAL = "8CB8BE"
        const val COLOR_COUNTDOWN_CAL = "D8CCA0"
        const val COLOR_ALL = "B6A4A0"
        const val COLOR_GOOGLE = "94A2C2"

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
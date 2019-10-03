package com.sandra.calendearlife.constant

import com.google.firebase.Timestamp
import com.sandra.calendearlife.constant.DateFormat.Companion.hour
import com.sandra.calendearlife.constant.DateFormat.Companion.minute
import java.text.SimpleDateFormat
import java.util.*

class DateFormat {
    companion object {

        private val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val monthOfYear = cal.get(Calendar.MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        const val BEGINTIME = "00:01"
        const val ENDTIME = "11:59"

    }
}

fun timeFormat2String4DatePicker(formatType: String, year: Int, month: Int, day: Int): String {

    val locale: Locale =
        if (Locale.getDefault().toString() == SharedPreferenceKey.CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    return SimpleDateFormat(formatType, locale).format(Date(
        year - 1900, month, day))
}

fun timeFormat2String4TimePicker(formatType: String, hour: Int, minute: Int): String {

    val locale: Locale =
        if (Locale.getDefault().toString() == SharedPreferenceKey.CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    return SimpleDateFormat(formatType, locale).format(Date(
        DateFormat.year - 1900,
        DateFormat.monthOfYear,
        DateFormat.dayOfMonth, hour, minute))
}

fun timeFormat2SqlTimestamp(formatType: String, parseString: String): java.sql.Timestamp {

    val locale: Locale =
        if (Locale.getDefault().toString() == SharedPreferenceKey.CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    return java.sql.Timestamp(SimpleDateFormat(formatType, locale).parse(parseString).time)
}

fun timeFormat2FirebaseTimestamp(formatType: String, parseString: String): Timestamp {

    val locale: Locale =
        if (Locale.getDefault().toString() == SharedPreferenceKey.CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    return Timestamp(SimpleDateFormat(formatType, locale).parse(parseString))
}

fun setDefaultTime(formatType: String): String {

    val locale: Locale =
        if (Locale.getDefault().toString() == SharedPreferenceKey.CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }
   return SimpleDateFormat(formatType, locale).format(Date(Timestamp.now().seconds * 1000))
}

fun transferTimestamp2String(formatType: String, dateSource: Timestamp): String {
    val locale: Locale =
        if (Locale.getDefault().toString() == SharedPreferenceKey.CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }
    return SimpleDateFormat(formatType, locale).format(dateSource.seconds * 1000)
}

const val TIME_FORMAT = "hh:mm a"
const val DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm a"
const val SIMPLE_DATE_FORMAT = "yyyy-MM-dd"
const val DATE_WEEK_FORMAT = "yyyy-MM-dd EEEE"
const val DATE_WEEK_TIME_FORMAT = "yyyy-MM-dd EEEE hh:mm a"





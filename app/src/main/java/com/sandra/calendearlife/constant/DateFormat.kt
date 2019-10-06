package com.sandra.calendearlife.constant

import com.google.firebase.Timestamp
import com.kizitonwose.calendarview.ui.MonthScrollListener
import com.sandra.calendearlife.calendar.month.CalendarMonthFragment
import com.sandra.calendearlife.constant.DateFormat.Companion.hour
import com.sandra.calendearlife.constant.DateFormat.Companion.minute
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.time.temporal.TemporalAccessor
import java.util.*

class DateFormat {
    companion object {

        private val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val monthOfYear = cal.get(Calendar.MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        const val BEGIN_TIME = "00:01"
        const val END_TIME = "11:59"

    }
}

fun getDefaultLocale() : Locale {
    return when {
        Locale.getDefault().toString() == SharedPreferenceKey.CHINESE -> Locale.TAIWAN
        else -> Locale.ENGLISH
    }
}

fun timeFormat2String4DatePicker(formatType: String, year: Int, month: Int, day: Int): String {

    return SimpleDateFormat(formatType, getDefaultLocale()).format(Date(
        year - 1900, month, day))
}

fun timeFormat2String4TimePicker(formatType: String, hour: Int, minute: Int): String {

    return SimpleDateFormat(formatType, getDefaultLocale()).format(Date(
        DateFormat.year - 1900,
        DateFormat.monthOfYear,
        DateFormat.dayOfMonth, hour, minute))
}

fun timeFormat2SqlTimestamp(formatType: String, parseString: String): java.sql.Timestamp {

    return java.sql.Timestamp(SimpleDateFormat(formatType, getDefaultLocale()).parse(parseString).time)
}

fun timeFormat2FirebaseTimestamp(formatType: String, parseString: String): Timestamp {

    return Timestamp(SimpleDateFormat(formatType, getDefaultLocale()).parse(parseString))
}

fun setDefaultTime(formatType: String): String {

   return SimpleDateFormat(formatType, getDefaultLocale()).format(Date(Timestamp.now().seconds * 1000))
}

fun transferTimestamp2String(formatType: String, dateSource: Timestamp): String {

    return SimpleDateFormat(formatType, getDefaultLocale()).format(dateSource.seconds * 1000)
}

fun timeSameYearFormatter(calendarMonth: org.threeten.bp.temporal.TemporalAccessor): String {

    return DateTimeFormatter.ofPattern(MONTH, getDefaultLocale()).format(calendarMonth)
}

fun calendarTitleFormatter(calendarMonth: org.threeten.bp.temporal.TemporalAccessor): String {

    return DateTimeFormatter.ofPattern(MONTH_YEAR, getDefaultLocale()).format(calendarMonth)
}

fun selectionDateFormatter(date: org.threeten.bp.temporal.TemporalAccessor): String {

    return DateTimeFormatter.ofPattern(DATE, getDefaultLocale()).format(date)
}

const val TIME_FORMAT = "hh:mm a"
const val DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm a"
const val SIMPLE_DATE_FORMAT = "yyyy-MM-dd"
const val DATE_WEEK_FORMAT = "yyyy-MM-dd EEEE"
const val DATE_WEEK_TIME_FORMAT = "yyyy-MM-dd EEEE hh:mm a"
const val MONTH = "MMM"
const val MONTH_YEAR = "MMM yyyy"
const val DATE = "yyyy-MMM-dd"





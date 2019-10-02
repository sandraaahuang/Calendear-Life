package com.sandra.calendearlife.constant

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.widget.TextView
import com.sandra.calendearlife.MyApplication
import java.text.SimpleDateFormat
import java.util.*

class DateFormat {
    companion object {

        val timeFormat = SimpleDateFormat("hh:mm a", Const.locale)
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Const.locale)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Const.locale)
        val dateWeekFormat = SimpleDateFormat("yyyy/MM/dd EEEE", Const.locale)
        val dateWeekTimeFormat = SimpleDateFormat("yyyy/MM/dd EEEE hh:mm a", Const.locale)

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
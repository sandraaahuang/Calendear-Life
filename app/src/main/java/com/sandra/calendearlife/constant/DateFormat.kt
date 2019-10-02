package com.sandra.calendearlife.constant

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
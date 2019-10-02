package com.sandra.calendearlife.constant

import java.text.SimpleDateFormat

class DateFormat {
    companion object {

        val timeFormat = SimpleDateFormat("hh:mm a", Const.locale)
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Const.locale)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Const.locale)
        val dateWeekFormat = SimpleDateFormat("yyyy/MM/dd EEEE", Const.locale)
        val dateWeekTimeFormat = SimpleDateFormat("yyyy/MM/dd EEEE hh:mm a", Const.locale)
    }
}
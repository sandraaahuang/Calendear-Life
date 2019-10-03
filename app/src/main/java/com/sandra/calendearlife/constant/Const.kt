package com.sandra.calendearlife.constant

import android.content.Context
import android.util.Log
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.util.FragmentType

class Const {
    companion object {
        // Google signIn code
        const val REQUEST_CODE = 12345
        const val RC_SIGN_IN = 12345

        // Frequency dialog
        var RESPONSE_EVALUATE = "response_evaluate"
        // Get dialog result data
        const val RESPONSE = "response"
        const val EVALUATE_DIALOG = "evaluate_dialog"
        const val REQUEST_EVALUATE = 0X110

        // Determine each fragment type
        const val TYPE_CALENDAR = "calendar"
        const val TYPE_HOME = "home"

        fun putType (type: String) {
            val preferences =
                MyApplication.instance.
                    getSharedPreferences("fragment", Context.MODE_PRIVATE)
            preferences.edit().putString("type", type).apply()
            preferences.getString("type","")
            FragmentType.type = type
            Log.d("sandraaa", "type = ${FragmentType.type}")
        }

        // frequency value
        var value: String = "Does not repeat"

        const val DOES_NOT_REPEAT = "Does not repeat"
        const val EVERY_DAY = "Every day"
        const val EVERY_WEEK = "Every week"
        const val EVERY_MONTH = "Every month"
        const val EVERY_YEAR = "Every year"

        // dialog TAG
        const val SHOW = "show"
    }
}
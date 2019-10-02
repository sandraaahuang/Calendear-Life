package com.sandra.calendearlife.constant

import android.content.Context
import android.util.Log
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.util.FragmentType
import java.util.*

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

        // Judge the language
        val locale: Locale =
            if (Locale.getDefault().toString() == "zh-rtw") {
                Locale.TAIWAN
            } else {
                Locale.ENGLISH
            }

        // Determine each fragment type
        fun putType (type: String) {
            val preferences =
                MyApplication.instance.
                    getSharedPreferences("fragment", Context.MODE_PRIVATE)
            preferences.edit().putString("type", type).apply()
            preferences.getString("type","")
            FragmentType.type = type
            Log.d("sandraaa", "type = ${FragmentType.type}")
        }
        const val TYPECALENDAR = "calendar"
        const val TYPEHOME = "home"

        // frequency value
        var value: String = "Does not repeat"
        var frequency = listOf("Does not repeat"
            , "Every day", "Every week", "Every month", "Every year")

    }
}
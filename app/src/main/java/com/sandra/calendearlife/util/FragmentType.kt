package com.sandra.calendearlife.util

import android.content.Context
import com.sandra.calendearlife.MyApplication

object FragmentType {
    private val preferences =
        MyApplication.instance.getSharedPreferences("fragment", Context.MODE_PRIVATE)

    var type: String? = null
        get() {

            return preferences.getString("type", null)
        }
        set(value) {
            field = preferences.edit().putString("type", value)?.apply().toString()
        }
}
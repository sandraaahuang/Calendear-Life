package com.sandra.calendearlife.util

import android.content.Context
import android.content.SharedPreferences
import com.sandra.calendearlife.MyApplication

object FragmentType {
    val preferences =
        MyApplication.instance.
            getSharedPreferences("fragment", Context.MODE_PRIVATE)

    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    var type: String? = null


        get() {

            return preferences.getString("type",null)
        }

        set(value) {
            field = preferences.edit().putString("type", value)?.apply().toString()
        }
}
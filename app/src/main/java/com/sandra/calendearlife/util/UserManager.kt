package com.sandra.calendearlife.util

import android.content.Context
import android.content.SharedPreferences
import com.sandra.calendearlife.MyApplication

object UserManager{
    val preferences =
        MyApplication.instance.
            getSharedPreferences("GoogleMessage", Context.MODE_PRIVATE)

    inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        operation(editor)
        editor.apply()
    }

    var id: String? = null


        get() {

            return preferences.getString("id",null)
        }

        set(value) {
            field = preferences.edit().putString("id", value)?.apply().toString()
        }

    var userName: String? = null


    get() {

        return preferences.getString("userName",null)
    }

    set(value) {
        field = preferences.edit().putString("userName", value)?.apply().toString()
    }

    var userEmail: String? = null


        get() {

            return preferences.getString("userEmail",null)
        }

        set(value) {
            field = preferences.edit().putString("userEmail", value)?.apply().toString()
        }


    var userPhoto: String? = null


        get() {

            return preferences.getString("userPhoto",null)
        }

        set(value) {
            field = preferences.edit().putString("userPhoto", value)?.apply().toString()
        }
}
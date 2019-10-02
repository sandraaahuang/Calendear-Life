package com.sandra.calendearlife.util

import android.content.Context
import com.sandra.calendearlife.MyApplication

object UserManager {
    private val preferences =
        MyApplication.instance.getSharedPreferences("GoogleLoginInfo", Context.MODE_PRIVATE)

    var id: String? = null
        get() {

            return preferences.getString("id", null)
        }
        set(value) {
            field = preferences.edit().putString("id", value)?.apply().toString()
        }

    var userName: String? = null
        get() {

            return preferences.getString("userName", null)
        }
        set(value) {
            field = preferences.edit().putString("userName", value)?.apply().toString()
        }

    var userEmail: String? = null
        get() {

            return preferences.getString("userEmail", null)
        }
        set(value) {
            field = preferences.edit().putString("userEmail", value)?.apply().toString()
        }


    var userPhoto: String? = null
        get() {

            return preferences.getString("userPhoto", null)
        }
        set(value) {
            field = preferences.edit().putString("userPhoto", value)?.apply().toString()
        }
}
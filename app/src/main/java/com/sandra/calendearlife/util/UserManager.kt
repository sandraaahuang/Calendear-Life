package com.sandra.calendearlife.util

import android.content.Context
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.GOOGLEINFO
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.ID
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.USEREMAIL
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.USERNAME
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.USERPHOTO

object UserManager {
    private val preferences =
        MyApplication.instance.getSharedPreferences(GOOGLEINFO, Context.MODE_PRIVATE)

    var id: String? = null
        get() {

            return preferences.getString(ID, null)
        }
        set(value) {
            field = preferences.edit().putString(ID, value)?.apply().toString()
        }

    var userName: String? = null
        get() {

            return preferences.getString(USERNAME, null)
        }
        set(value) {
            field = preferences.edit().putString(USERNAME, value)?.apply().toString()
        }

    var userEmail: String? = null
        get() {

            return preferences.getString(USEREMAIL, null)
        }
        set(value) {
            field = preferences.edit().putString(USEREMAIL, value)?.apply().toString()
        }


    var userPhoto: String? = null
        get() {

            return preferences.getString(USERPHOTO, null)
        }
        set(value) {
            field = preferences.edit().putString(USERPHOTO, value)?.apply().toString()
        }

    val isLoggedIn: Boolean
        get() = id != null
}
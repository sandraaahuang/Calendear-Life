package com.sandra.calendearlife.constant

class SharedPreferenceKey {
    companion object {

        // User info
        // name
        const val GOOGLEINFO = "GoogleLoginInfo"
        // extra key
        const val ID = "id"
        const val USERNAME = "userName"
        const val USEREMAIL = "userEmail"
        const val USERPHOTO = "userPhoto"

        // Widget
        // extra key
        const val POSITION = "position"
        const val REFRESHID = "refreshId"
        const val REMINDERSITEM = "remindersItem"
        const val TURN = "turn"
        // value
        const val LOGIN = "login"
        const val ADDFRAGMENT = "addFragment"
        // action
        const val CLICK = "click"

        // Dark mode
        // name
        const val DARKMODE = "DarkMode"
        // extra key
        const val STATUS = "status"
        // value
        const val DARK = "dark"
        const val LIGHT = "light"

        // language
        // name
        const val SETTINGS = "Settings"
        // extra key
        const val LANG = "Lang"
        // value
        const val CHINESE = "zh-rtw"
        const val ENGLISH = "en"
        const val ZH = "zh"

        // Alarm intent action
        const val COUNTDOWN = "countdown"
        const val DNR = "dnr"
        const val ED = "ED"
        const val EW = "EW"
        const val EM = "EM"
        const val EY = "EY"
    }
}
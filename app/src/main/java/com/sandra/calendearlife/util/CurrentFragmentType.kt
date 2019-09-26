package com.sandra.calendearlife.util

import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R




enum class CurrentFragmentType(var value: String) {
    PREVIEW(""),
    HOME(getString(R.string.home)),
    DETAIL(getString(R.string.detail)),
    NEWEVENT(getString(R.string.add_event_into_calendar)),
    NEWREMINDER(getString(R.string.add_event_into_reminder)),
    NEWCOUNTDOWN(getString(R.string.add_event_into_countdown)),
    HISTORY(getString(R.string.history))

}

fun getString(resourceId: Int): String {
    return MyApplication.instance.getString(resourceId)
}

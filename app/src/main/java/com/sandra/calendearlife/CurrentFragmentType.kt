package com.sandra.calendearlife


enum class CurrentFragmentType(val value: String) {
    PREVIEW(""),
    HOME(getString(R.string.home)),
    DETAIL(getString(R.string.detail)),
    NEWEVENT(getString(R.string.add_event_into_calendar)),
    NEWREMINDER(getString(R.string.add_event_into_reminder)),
    NEWCOUNTDOWN(getString(R.string.add_event_into_countdown)),
    SCHEDULE(getString(R.string.schedule)),
    DAY(getString(R.string.day_fragment)),
    WEEK(getString(R.string.week_fragment)),
    SEARCH(getString(R.string.search_fragment))
}

fun getString(resourceId: Int): String {
    return MyApplication.instance.getString(resourceId)
}

package com.sandra.calendearlife.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Database(
    val data: Data
) : Parcelable

@Parcelize
data class Data (
    val gmail: String? = null,
    val hasAccount: Boolean = false,
    val calendar: ArrayList<Calendar>? = null
): Parcelable

@Parcelize
data class Calendar (
    val setDate: String? = null,
    val beginDate: String? = null,
    val endDate: String? = null,
    val title: String? = null,
    val note: String? = null,
    val hasGuests: Boolean = false,
    val guest: ArrayList<String>,
    val isAllDay: Boolean = false,
    val hasLocation: Boolean = false,
    val location: String? = null,
    val hasReminders: Boolean = false,
    val reminders: List<Reminders>? = null,
    val hasCountdown: Boolean = false,
    val countdowns: List<Countdown>? = null,
    val documentID: String? = null

) : Parcelable

@Parcelize
data class Reminders(
    val setDate: String? = null,
    val title: String? = null,
    val setRemindDate: Boolean = false,
    val remindDate: String? = null,
    val isChecked: Boolean = false,
    val note: String? = null,
    val frequency: String? = null,
    val documentID: String
) : Parcelable

@Parcelize
data class Countdown(
    val setDate: String? = null,
    val title: String? = null,
    val note: String? = null,
    val targetDate: String? = null,
    val overdue: Boolean = false,
    val documentID: String
) : Parcelable
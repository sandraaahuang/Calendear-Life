package com.sandra.calendearlife.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Calendar (
    val color: String? = null,
    val date: String? = null,
    val setDate: String? = null,
    val beginDate: String? = null,
    val endDate: String? = null,
    val title: String? = null,
    val note: String? = null,
    val isAllDay: Boolean = false,
    val hasLocation: Boolean = false,
    val location: String? = null,
    val hasReminders: Boolean = false,
    val hasCountdown: Boolean = false,
    val documentID: String? = null,
    val frequency: String? = null,
    val fromGoogle: Boolean = false
) : Parcelable

@Parcelize
data class Reminders(
    val setDate: String? = null,
    val title: String? = null,
    val setRemindDate: Boolean = false,
    val remindDate: String? = null,
    val remindTimestamp: Timestamp,
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
    val targetTimestamp: Timestamp,
    val overdue: Boolean = false,
    val documentID: String
) : Parcelable
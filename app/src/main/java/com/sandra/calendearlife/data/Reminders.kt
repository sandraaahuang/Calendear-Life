package com.sandra.calendearlife.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Reminders(
    val title: String,
    val remindDate: String,
    val checked: Boolean? = false,
    val timeChecked: Boolean? = false
) : Parcelable
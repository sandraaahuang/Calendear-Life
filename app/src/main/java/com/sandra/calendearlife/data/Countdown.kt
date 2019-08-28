package com.sandra.calendearlife.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Countdown(
    val date: String,
    val title: String,
    val targetDate: String
) : Parcelable
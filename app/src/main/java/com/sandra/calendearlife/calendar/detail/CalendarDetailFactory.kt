package com.sandra.calendearlife.calendar.detail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sandra.calendearlife.data.Calendar

class CalendarDetailFactory(
    private val calendar: Calendar,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarDetailViewModel::class.java)) {
            return CalendarDetailViewModel(calendar, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
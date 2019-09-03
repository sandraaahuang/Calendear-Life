package com.sandra.calendearlife.reminders

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sandra.calendearlife.data.Reminders

class DetailViewModelFactory(
    private val reminders: Reminders,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemindersDetailViewModel::class.java)) {
            return RemindersDetailViewModel(reminders, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
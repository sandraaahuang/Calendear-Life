package com.sandra.calendearlife.countdown

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sandra.calendearlife.data.Countdown

class CountdownDetailFactory(
    private val countdown: Countdown,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CountdownDetailViewModel::class.java)) {
            return CountdownDetailViewModel(countdown, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
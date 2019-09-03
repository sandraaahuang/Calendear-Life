package com.sandra.calendearlife.countdown

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sandra.calendearlife.data.Countdown

class CountdownDetailViewModel(countdown: Countdown,app: Application) : AndroidViewModel(app) {


    private val _selectedItem = MutableLiveData<Countdown>()

    val selectedItem: LiveData<Countdown>
        get() = _selectedItem

    init {
        _selectedItem.value = countdown
    }


}
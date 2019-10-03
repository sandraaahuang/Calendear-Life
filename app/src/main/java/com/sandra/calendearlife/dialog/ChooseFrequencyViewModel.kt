package com.sandra.calendearlife.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChooseFrequencyViewModel : ViewModel() {

    private var _chooseFrequency = MutableLiveData<String>()
    val chooseFrequency: LiveData<String>
        get() = _chooseFrequency

    fun chooseFrequency(frequency: String) {
        _chooseFrequency.value = frequency
    }

}
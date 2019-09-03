package com.sandra.calendearlife.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.sandra.calendearlife.data.Reminders

class RemindersDetailViewModel(reminders: Reminders,app: Application) : AndroidViewModel(app) {


    private val _selectedItem = MutableLiveData<Reminders>()

    val selectedItem: LiveData<Reminders>
        get() = _selectedItem

    init {
        _selectedItem.value = reminders
    }


}
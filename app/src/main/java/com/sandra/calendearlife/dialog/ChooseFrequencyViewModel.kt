package com.sandra.calendearlife.dialog

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sandra.calendearlife.R

class ChooseFrequencyViewModel : ViewModel() {

    private var _chooseFrequency = MutableLiveData<String>()
    val chooseFrequency: LiveData<String>
        get() = _chooseFrequency

    fun onButtonClick(view: View) {
        when (view.id) {
            R.id.iconBack -> chooseFrequency(view.context.getString(R.string.does_not_repeat))
            R.id.buttonDoesNotRepeat -> chooseFrequency(view.context.getString(R.string.does_not_repeat))
            R.id.buttonEveryDay -> chooseFrequency(view.context.getString(R.string.every_day))
            R.id.buttonEveryMonth -> chooseFrequency(view.context.getString(R.string.every_month))
            R.id.buttonEveryWeek -> chooseFrequency(view.context.getString(R.string.every_week))
            R.id.buttonEveryYear -> chooseFrequency(view.context.getString(R.string.every_year))
        }
    }

    private fun chooseFrequency(frequency: String) {
        _chooseFrequency.value = frequency
    }

}
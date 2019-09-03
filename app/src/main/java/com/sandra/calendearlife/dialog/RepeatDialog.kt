package com.sandra.calendearlife.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.MutableLiveData
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.DialogRepeatBinding
import com.sandra.calendearlife.reminders.RemindersViewModel

class RepeatDialog: AppCompatDialogFragment() {

    lateinit var binding: DialogRepeatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME , R.style.MessageDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
                              , savedInstanceState: Bundle?)
            : View? {

        binding = DialogRepeatBinding.inflate(inflater, container, false)

        binding.buttonBack.setOnClickListener {
            this.dismiss()
        }

        binding.buttonDoesNotRepeat.setOnClickListener {
            value?.value = frequency[0]
            Log.d("sandraaa","value = ${RemindersViewModel().displayChoose.value}")
            this.dismiss()
        }
        binding.buttonEveryDay.setOnClickListener {
            value?.value = frequency[1]
            RemindersViewModel().showValue()
            this.dismiss()
        }
        binding.buttonEveryWeek.setOnClickListener {
            value?.value = frequency[2]
            RemindersViewModel().showValue()
            this.dismiss()
        }
        binding.buttonEveryMonth.setOnClickListener {
            value?.value = frequency[3]
            RemindersViewModel().showValue()
            this.dismiss()
        }
        binding.buttonEveryYear.setOnClickListener {
            value?.value = frequency[4]
            RemindersViewModel().showValue()
            this.dismiss()
        }

        return binding.root
    }

    companion object {
        var value: MutableLiveData<String>? = null
        var frequency = listOf("does not repeat"
            , "every day", "every week", "every month", "every year")
    }

}
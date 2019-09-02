package com.sandra.calendearlife.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.DialogRepeatBinding

class RepeatDialog: AppCompatDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME , R.style.MessageDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
                              , savedInstanceState: Bundle?)
            : View? {

        val binding = DialogRepeatBinding.inflate(inflater, container, false)

        binding.buttonBack.setOnClickListener {
            this.dismiss()
        }

        binding.buttonDoesNotRepeat.setOnClickListener {
            value = frequency[0]
            this.dismiss()
        }
        binding.buttonEveryDay.setOnClickListener {
            value = frequency[1]
            this.dismiss()
        }
        binding.buttonEveryWeek.setOnClickListener {
            value = frequency[2]
            this.dismiss()
        }
        binding.buttonEveryMonth.setOnClickListener {
            value = frequency[3]
            this.dismiss()
        }
        binding.buttonEveryYear.setOnClickListener {
            value = frequency[4]
            this.dismiss()
        }

        return binding.root
    }

    companion object {
        lateinit var value: String
        var frequency = listOf("does not repeat"
            , "every day", "every week", "every month", "every year")
    }

}
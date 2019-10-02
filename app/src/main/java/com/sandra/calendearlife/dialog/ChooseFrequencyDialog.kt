package com.sandra.calendearlife.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.DialogChooseFrequencyBinding
import com.sandra.calendearlife.constant.Const.Companion.REQUEST_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.frequency
import com.sandra.calendearlife.constant.Const.Companion.value


class ChooseFrequencyDialog: AppCompatDialogFragment() {

    private fun setResult(){
        if (targetFragment == null){
            return
        }
        else {
            val intent = Intent()
            intent.putExtra(RESPONSE_EVALUATE, value)
            targetFragment?.onActivityResult(REQUEST_EVALUATE, Activity.RESULT_OK,intent)
        }
    }

    lateinit var binding: DialogChooseFrequencyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME , R.style.MessageDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
                              , savedInstanceState: Bundle?)
            : View? {

        binding = DialogChooseFrequencyBinding.inflate(inflater, container, false)

        binding.buttonBack.setOnClickListener {
            value = frequency[0]
            setResult()
            this.dismiss()
        }

        binding.buttonDoesNotRepeat.setOnClickListener {
            value = frequency[0]
            setResult()
            this.dismiss()
        }
        binding.buttonEveryDay.setOnClickListener {
            value = frequency[1]
            setResult()
            this.dismiss()
        }
        binding.buttonEveryWeek.setOnClickListener {
            value = frequency[2]
            setResult()
            this.dismiss()
        }
        binding.buttonEveryMonth.setOnClickListener {
            value = frequency[3]
            setResult()
            this.dismiss()
        }
        binding.buttonEveryYear.setOnClickListener {
            value = frequency[4]
            setResult()
            this.dismiss()
        }
        return binding.root
    }
}
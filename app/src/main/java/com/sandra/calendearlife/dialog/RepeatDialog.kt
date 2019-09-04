package com.sandra.calendearlife.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.sandra.calendearlife.R
import com.sandra.calendearlife.calendar.CalendarEventFragment
import com.sandra.calendearlife.databinding.DialogRepeatBinding
import com.sandra.calendearlife.reminders.RemindersFragment
import com.sandra.calendearlife.reminders.RemindersViewModel



class RepeatDialog: AppCompatDialogFragment() {

    var RESPONSE_EVALUATE = "response_evaluate"
    var RESPONSE_EVALUATE2 = "response_evaluate"

    fun setResult(){
        if (targetFragment == null){
            Log.d("sandraaa", "fail")
            return
        }
        else {
            val intent = Intent()
            intent.putExtra(RESPONSE_EVALUATE, value)
            targetFragment?.onActivityResult(RemindersFragment().REQUEST_EVALUATE, Activity.RESULT_OK,intent)
            Log.d("sandraaa", "value = $value")
        }
    }

    fun setResult2(){
        if (targetFragment == null){
            Log.d("sandraaa", "fail")
            return
        }
        else {
            val intent = Intent()
            intent.putExtra(RESPONSE_EVALUATE2, value)
            targetFragment?.onActivityResult(CalendarEventFragment().REQUEST_EVALUATE, Activity.RESULT_OK,intent)
            Log.d("sandraaa", "value = $value")
        }
    }

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
            value = frequency[0]
            setResult()
            setResult2()
            this.dismiss()
        }

        binding.buttonDoesNotRepeat.setOnClickListener {
            value = frequency[0]
            setResult()
            setResult2()
            this.dismiss()
        }
        binding.buttonEveryDay.setOnClickListener {
            value = frequency[1]
            setResult()
            setResult2()
            this.dismiss()
        }
        binding.buttonEveryWeek.setOnClickListener {
            value = frequency[2]
            setResult()
            setResult2()
            this.dismiss()
        }
        binding.buttonEveryMonth.setOnClickListener {
            value = frequency[3]
            setResult()
            setResult2()
            this.dismiss()
        }
        binding.buttonEveryYear.setOnClickListener {
            value = frequency[4]
            setResult()
            setResult2()
            this.dismiss()
        }
        return binding.root
    }

    companion object {
        lateinit var value: String
        var frequency = listOf("Does not repeat"
            , "Every day", "Every week", "Every month", "Every year")
    }

}
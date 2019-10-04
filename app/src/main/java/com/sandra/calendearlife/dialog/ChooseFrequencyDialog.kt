package com.sandra.calendearlife.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.Const.Companion.REQUEST_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE_EVALUATE
import com.sandra.calendearlife.databinding.DialogChooseFrequencyBinding
import com.sandra.calendearlife.util.Logger


class ChooseFrequencyDialog: AppCompatDialogFragment() {

    lateinit var binding: DialogChooseFrequencyBinding

    private val viewModel: ChooseFrequencyViewModel by lazy {
        ViewModelProviders.of(this).get(ChooseFrequencyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME , R.style.MessageDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
                              , savedInstanceState: Bundle?)
            : View? {

        binding = DialogChooseFrequencyBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        viewModel.chooseFrequency.observe(this, Observer { frequencyValue ->
            frequencyValue?.let {
                setResult(it)
                this.dismiss()
            }
        })

        return binding.root
    }

    private fun setResult(frequency: String) {
        if (targetFragment == null) {
            Logger.d("failed")
            return
        }
        else {
            val intent = Intent()
            intent.putExtra(RESPONSE_EVALUATE, frequency)
            targetFragment?.onActivityResult(REQUEST_EVALUATE, Activity.RESULT_OK,intent)
        }
    }
}
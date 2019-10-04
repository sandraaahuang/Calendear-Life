package com.sandra.calendearlife.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.*
import com.sandra.calendearlife.constant.Const.Companion.SHOW
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGIN_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.databinding.FragmentRemindersDetailBinding
import com.sandra.calendearlife.dialog.DiscardDialog


class RemindersDetailFragment : Fragment() {

    private lateinit var binding: FragmentRemindersDetailBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val remindersProperty = RemindersDetailFragmentArgs.fromBundle(arguments!!).remindersProperty
        val viewModelFactory = DetailViewModelFactory(remindersProperty, requireNotNull(activity).application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(RemindersDetailViewModel::class.java)

        binding = FragmentRemindersDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.switchRemindDay.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                binding.remindLayout.visibility = View.VISIBLE
            } else {
                binding.remindLayout.visibility = View.GONE
            }
        }

        binding.saveButton2.setOnClickListener {

            val updateItem = hashMapOf(
                TITLE to "${binding.remindersTitle.text}".trim(),
                NOTE to "${binding.editTextRemindNote.text}".trim(),
                REMIND_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT,
                    "${binding.remindDate.text} ${binding.remindTime.text}" )
            )

            val calendarItem = hashMapOf(
                TITLE to "${binding.remindersTitle.text}".trim(),
                NOTE to "${binding.editTextRemindNote.text}".trim(),
                BEGIN_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT,
                    "${binding.remindDate.text} ${binding.remindTime.text}" ),
                DATE to timeFormat2SqlTimestamp(
                    SIMPLE_DATE_FORMAT, "${binding.remindDate.text}" )
            )

            viewModel.updateItem(updateItem, calendarItem, remindersProperty.documentID)

            view?.let {
                Snackbar.make(it, getString(R.string.update_message), Snackbar.LENGTH_LONG).show()
            }

        }

        binding.deleteButton2.setOnClickListener {
            viewModel.deleteItem(remindersProperty.documentID)

            view?.let {
                Snackbar.make(it, getString(R.string.delete_message), Snackbar.LENGTH_LONG).show()
            }

        }

        viewModel.isUpdateCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                findNavController().navigate(NavigationDirections
                    .actionGlobalHomeFragment())
            }
        })

        viewModel.isClicked.observe(this, androidx.lifecycle.Observer {
            it?.let {
                binding.saveButton2.isClickable = false
                binding.deleteButton2.isClickable = false
            }
        })

        viewModel.showDatePicker.observe(this, androidx.lifecycle.Observer { clickedText ->
            clickedText?.let {
                showDatePicker(it)
            }
        })

        viewModel.showTimePicker.observe(this, androidx.lifecycle.Observer { clickedText ->
            clickedText?.let {
                showTimePicker(it)
            }
        })

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@RemindersDetailFragment.fragmentManager?.let {
                    DiscardDialog().show(it, SHOW)
                }

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            this.fragmentManager?.let {
                DiscardDialog().show(it, SHOW)
            }

        }

        return binding.root
    }

    private fun showDatePicker(inputDate: TextView) {
        DatePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
            { _, year, monthOfYear, dayOfMonth ->
                inputDate.text = timeFormat2String4DatePicker(SIMPLE_DATE_FORMAT, year, monthOfYear, dayOfMonth)
            }, DateFormat.year, DateFormat.monthOfYear, DateFormat.dayOfMonth
        ).show()
    }

    private fun showTimePicker(inputTime: TextView) {
        TimePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { _, hour, minute ->
                inputTime.text = timeFormat2String4TimePicker(TIME_FORMAT, hour, minute)
            }, DateFormat.hour, DateFormat.minute, false
        ).show()
    }
}




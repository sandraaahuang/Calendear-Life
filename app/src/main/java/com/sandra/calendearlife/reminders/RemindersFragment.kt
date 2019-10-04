package com.sandra.calendearlife.reminders

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.*
import com.sandra.calendearlife.constant.Const.Companion.EVALUATE_DIALOG
import com.sandra.calendearlife.constant.Const.Companion.REQUEST_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.SHOW
import com.sandra.calendearlife.constant.Const.Companion.value
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGIN_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.END_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROM_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.databinding.FragmentRemindersBinding
import com.sandra.calendearlife.dialog.ChooseFrequencyDialog
import com.sandra.calendearlife.dialog.DiscardDialog


class RemindersFragment : Fragment() {

    private val viewModel: RemindersViewModel by lazy {
        ViewModelProviders.of(this).get(RemindersViewModel::class.java)
    }

    lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.setReminderswitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.setRemindLayout.visibility = View.VISIBLE
            } else {
                binding.setRemindLayout.visibility = View.GONE
            }
        }

        binding.repeatChoose.setOnClickListener {
            val dialog = ChooseFrequencyDialog()
            dialog.setTargetFragment(this, REQUEST_EVALUATE);
            dialog.show(fragmentManager!!, EVALUATE_DIALOG)
        }

        binding.remindersDateInput.text = setDefaultTime(SIMPLE_DATE_FORMAT)
        binding.remindersTimeInput.text = setDefaultTime(TIME_FORMAT)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@RemindersFragment.fragmentManager?.let {
                    DiscardDialog().show(it, SHOW)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            fragmentManager?.let {
                DiscardDialog().show(it, SHOW)
            }
        }

        binding.saveText.setOnClickListener {

            val calendar = hashMapOf(
                COLOR to COLOR_REMIND,
                SET_DATE to FieldValue.serverTimestamp(),
                BEGIN_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT,
                    "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"),
                END_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT,
                    "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"),
                DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, "${binding.remindersDateInput.text}"),
                TITLE to "${binding.remindersTitleInput.text}".trim(),
                NOTE to "${binding.remindersNoteInput.text}".trim(),
                HAS_REMINDERS to true,
                FREQUENCY to value,
                FROM_GOOGLE to false,
                LOCATION to "",
                REMINDERS_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT,
                    "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}")
            )

            val reminders = hashMapOf(
                SET_DATE to FieldValue.serverTimestamp(),
                TITLE to "${binding.remindersTitleInput.text}".trim(),
                HAS_REMIND_DATE to binding.setReminderswitch.isChecked,
                REMIND_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT,
                    "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"),
                IS_CHECKED to false,
                NOTE to "${binding.remindersNoteInput.text}".trim(),
                FREQUENCY to value
            )

            if ("${binding.remindersTitleInput.text}" == "") {
                binding.remindersTitleInput.setHintTextColor(resources.getColor(R.color.delete_red))
            } else {
                viewModel.writeItem(calendar, reminders)
                Snackbar.make(this.view!!, getString(R.string.save_message), Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isUpdateCompleted.observe(this, androidx.lifecycle.Observer {
            restartApp()
        })

        viewModel.isClicked.observe(this, androidx.lifecycle.Observer {
            it?.let {
                binding.saveText.isClickable = false
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

        return binding.root
    }

    private fun restartApp() {

        val intent = Intent(this.context, MainActivity::class.java)
        startActivity(intent)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EVALUATE) {

            binding.repeatChoose.text = data?.getStringExtra(RESPONSE_EVALUATE)
            Intent().putExtra(RESPONSE, data?.getStringExtra(RESPONSE_EVALUATE))
            activity?.setResult(Activity.RESULT_OK, Intent())
        }
    }

    private fun showDatePicker(inputDate: TextView) {
        context?.let {
            DatePickerDialog(
                it, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    inputDate.text = timeFormat2String4DatePicker(SIMPLE_DATE_FORMAT, year, monthOfYear, dayOfMonth)
                }, DateFormat.year, DateFormat.monthOfYear, DateFormat.dayOfMonth
            ).show()
        }
    }

    private fun showTimePicker(inputTime: TextView) {
        context?.let {
            TimePickerDialog(
                it, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
                { _, hour, minute ->
                    inputTime.text = timeFormat2String4TimePicker(TIME_FORMAT, hour, minute)
                }, DateFormat.hour, DateFormat.minute, false
            ).show()
        }
    }

}










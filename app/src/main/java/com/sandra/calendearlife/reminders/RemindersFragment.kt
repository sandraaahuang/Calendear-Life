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
import com.sandra.calendearlife.constant.Const
import com.sandra.calendearlife.constant.Const.Companion.EVALUATE_DIALOG
import com.sandra.calendearlife.constant.Const.Companion.REQUEST_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.value
import com.sandra.calendearlife.constant.DateFormat
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ENDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROMGOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASREMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ISCHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETREMINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.databinding.FragmentRemindersBinding
import com.sandra.calendearlife.dialog.ChooseFrequencyDialog
import com.sandra.calendearlife.dialog.DiscardDialog
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class RemindersFragment : Fragment() {

    private val viewModel: RemindersViewModel by lazy {
        ViewModelProviders.of(this).get(RemindersViewModel::class.java)
    }

    val locale: Locale =
        if (Locale.getDefault().toString() == CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }
    private val timeFormat = SimpleDateFormat("hh:mm a", locale)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", locale)

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

        binding.remindersDateInput.text =
            simpleDateFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))
        binding.remindersTimeInput.text = timeFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DiscardDialog().show(this@RemindersFragment.fragmentManager!!, "show")
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }

        binding.saveText.setOnClickListener {
            val remindDate = "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"
            val date = "${binding.remindersDateInput.text}"

            val calendar = hashMapOf(
                COLOR to COLOR_REMIND,
                SETDATE to FieldValue.serverTimestamp(),
                BEGINDATE to Timestamp(dateTimeFormat.parse(remindDate).time),
                ENDDATE to Timestamp(dateTimeFormat.parse(remindDate).time),
                DATE to Timestamp(simpleDateFormat.parse(date).time),
                TITLE to "${binding.remindersTitleInput.text}".trim(),
                NOTE to "${binding.remindersNoteInput.text}".trim(),
                HASREMINDERS to true,
                FREQUENCY to value,
                FROMGOOGLE to false
            )

            val reminders = hashMapOf(
                SETDATE to FieldValue.serverTimestamp(),
                TITLE to "${binding.remindersTitleInput.text}".trim(),
                SETREMINDATE to binding.setReminderswitch.isChecked,
                REMINDDATE to Timestamp(dateTimeFormat.parse(remindDate).time),
                ISCHECKED to false,
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

        viewModel.showDatePicker.observe(this, androidx.lifecycle.Observer {
            it?.let {
                showDatePicker(it)
            }
        })

        viewModel.showTimePicker.observe(this, androidx.lifecycle.Observer {
            it?.let {
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
            val evaluate = data?.getStringExtra(RESPONSE_EVALUATE)

            binding.repeatChoose.text = evaluate
            val intent = Intent()
            intent.putExtra(RESPONSE, evaluate);
            activity?.setResult(Activity.RESULT_OK, intent)
        }
    }

    private fun showDatePicker(inputDate: TextView) {
        val datePickerDialog = DatePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
            { _, year, monthOfYear, dayOfMonth ->
                inputDate.text =
                    simpleDateFormat.format(Date(year - 1900, monthOfYear, dayOfMonth))
            }, DateFormat.year, DateFormat.monthOfYear, DateFormat.dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(inputTime: TextView) {
        val timePickerDialog = TimePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { _, hour, minute ->
                inputTime.text =
                    timeFormat.format(Date(
                        DateFormat.year - 1900,
                        DateFormat.monthOfYear,
                        DateFormat.dayOfMonth, hour, minute))
            }, DateFormat.hour, DateFormat.minute, false
        )
        timePickerDialog.show()
    }

}










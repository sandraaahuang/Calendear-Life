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
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.FragmentRemindersBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.dialog.ChooseFrequencyDialog
import com.sandra.calendearlife.constant.Const.Companion.EVALUATE_DIALOG
import com.sandra.calendearlife.constant.Const.Companion.REQUEST_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.value
import com.sandra.calendearlife.constant.DateFormat.Companion.dateTimeFormat
import com.sandra.calendearlife.constant.DateFormat.Companion.simpleDateFormat
import com.sandra.calendearlife.constant.DateFormat.Companion.timeFormat
import java.sql.Timestamp
import java.util.*


class RemindersFragment : Fragment() {

    private val viewModel: RemindersViewModel by lazy {
        ViewModelProviders.of(this).get(RemindersViewModel::class.java)
    }

    lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

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

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val monthOfYear = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)



        binding.remindersDateInput.text =
            simpleDateFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))
        binding.remindersTimeInput.text = timeFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))

        binding.remindersDateInput.setOnClickListener {

            val datePickerDialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    // Display Selected setDate in textbox
                    val date = Date(year - 1900, monthOfYear, dayOfMonth)
                    val stringTime = simpleDateFormat.format(date)
                    binding.remindersDateInput.text =
                        "$stringTime"
                }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.remindersTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            TimePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
                { view, hour, minute ->
                    val date = Date(year, monthOfYear, dayOfMonth, hour, minute)
                    val stringTime = timeFormat.format(date)
                    binding.remindersTimeInput.text =
                        "$stringTime"
                }, hour, minute, false
            ).show()
        }

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
            val parsedDate = dateTimeFormat.parse(remindDate)
            val parsed = simpleDateFormat.parse(date)

            val calendar = hashMapOf(
                "color" to "C02942",
                "setDate" to FieldValue.serverTimestamp(),
                "beginDate" to Timestamp(parsedDate.time),
                "endDate" to Timestamp(parsedDate.time),
                "date" to Timestamp(parsed.time),
                "title" to "${binding.remindersTitleInput.text}".trim(),
                "note" to "${binding.remindersNoteInput.text}".trim(),
                "hasReminders" to true,
                "frequency" to value,
                "fromGoogle" to false
            )

            val reminders = hashMapOf(
                "setDate" to FieldValue.serverTimestamp(),
                "title" to "${binding.remindersTitleInput.text}".trim(),
                "setRemindDate" to binding.setReminderswitch.isChecked,
                "remindDate" to Timestamp(parsedDate.time),
                "isChecked" to false,
                "note" to "${binding.remindersNoteInput.text}".trim(),
                "frequency" to value
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

}










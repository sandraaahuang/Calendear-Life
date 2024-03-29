package com.sandra.calendearlife.calendar.event

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.*
import com.sandra.calendearlife.constant.Const.Companion.EVALUATE_DIALOG
import com.sandra.calendearlife.constant.Const.Companion.REQUEST_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.SHOW
import com.sandra.calendearlife.constant.Const.Companion.value
import com.sandra.calendearlife.constant.DateFormat.Companion.BEGIN_TIME
import com.sandra.calendearlife.constant.DateFormat.Companion.END_TIME
import com.sandra.calendearlife.constant.DateFormat.Companion.dayOfMonth
import com.sandra.calendearlife.constant.DateFormat.Companion.hour
import com.sandra.calendearlife.constant.DateFormat.Companion.minute
import com.sandra.calendearlife.constant.DateFormat.Companion.monthOfYear
import com.sandra.calendearlife.constant.DateFormat.Companion.year
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGIN_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.END_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROM_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_ALL_DAY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_CHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.IS_OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_REMIND_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDERS_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.databinding.FragmentCalendarEventBinding
import com.sandra.calendearlife.dialog.ChooseFrequencyDialog
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.util.Logger


class CalendarEventFragment : Fragment() {

    lateinit var binding: FragmentCalendarEventBinding

    private val viewModel: CalendarEventViewModel by lazy {
        ViewModelProviders.of(this).get(CalendarEventViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCalendarEventBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        binding.allDaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.endDate.visibility = View.GONE
                binding.beginTime.visibility = View.GONE
                binding.endTime.visibility = View.GONE
            } else {
                binding.endDate.visibility = View.VISIBLE
                binding.beginTime.visibility = View.VISIBLE
                binding.endTime.visibility = View.VISIBLE
            }
        }

        binding.switchSetAsReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.setRemindLayout.visibility = View.VISIBLE
            } else {
                binding.setRemindLayout.visibility = View.GONE
            }
        }

        binding.repeatChoose.setOnClickListener {
            val dialog = ChooseFrequencyDialog()
            //setTargetFragment
            dialog.setTargetFragment(this, REQUEST_EVALUATE)
            fragmentManager?.let {
                dialog.show(it, EVALUATE_DIALOG)
            }
        }

        setDefaultDate()

        viewModel.showDateWeekPicker.observe(this, androidx.lifecycle.Observer { clickedDate ->
            clickedDate?.let {
                showDatePickerInWeekFormat(it)
            } }
        )

        viewModel.showDatePicker.observe(this, androidx.lifecycle.Observer { clickedDate ->
            clickedDate?.let {
                showDatePicker(it)
            }
        })

        viewModel.showTimePicker.observe(this, androidx.lifecycle.Observer { clickedDate ->
            clickedDate?.let {
                showTimePicker(it)
            }
        })

        viewModel.hasPermission.observe(this, androidx.lifecycle.Observer {
            it?.let {
                requestPermission()
            }
        })

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@CalendarEventFragment.fragmentManager?.let {
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
            // error handle
            if (binding.eventTitleInput.text.isNullOrEmpty()) {
                binding.eventTitleInput.setHintTextColor(resources.getColor(R.color.delete_red))
            } else {
                val date: String
                val beginDate: String
                val endDate: String

                if (binding.allDaySwitch.isChecked) {
                    date = "${binding.beginDate.text}"
                    beginDate = "${binding.beginDate.text} $BEGIN_TIME ${getString(R.string.am)}"
                    endDate = "${binding.beginDate.text} $END_TIME ${getString(R.string.pm)}"
                } else {
                    date = "${binding.beginDate.text}"
                    beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                    endDate = "${binding.endDate.text} ${binding.endTime.text}"
                }

                val remindDate = "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"

                val countdown = hashMapOf(
                    SET_DATE to FieldValue.serverTimestamp(),
                    TITLE to "${binding.eventTitleInput.text}".trim(),
                    NOTE to "${binding.noteInput.text}".trim(),
                    TARGET_DATE to timeFormat2SqlTimestamp(DATE_WEEK_TIME_FORMAT, endDate),
                    IS_OVERDUE to false
                )

                val reminders = hashMapOf(
                    SET_DATE to FieldValue.serverTimestamp(),
                    TITLE to "${binding.eventTitleInput.text}".trim(),
                    HAS_REMIND_DATE to true,
                    REMIND_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, remindDate),
                    IS_CHECKED to false,
                    NOTE to "${binding.noteInput.text}".trim(),
                    FREQUENCY to value
                )

                val item = hashMapOf(
                    FREQUENCY to value,
                    DATE to timeFormat2SqlTimestamp(DATE_WEEK_FORMAT, date),
                    SET_DATE to FieldValue.serverTimestamp(),
                    BEGIN_DATE to timeFormat2SqlTimestamp(DATE_WEEK_TIME_FORMAT, beginDate),
                    END_DATE to timeFormat2SqlTimestamp(DATE_WEEK_TIME_FORMAT, endDate),
                    TITLE to "${binding.eventTitleInput.text}".trim(),
                    NOTE to "${binding.noteInput.text}".trim(),
                    IS_ALL_DAY to "${binding.allDaySwitch.isChecked}",
                    HAS_REMINDERS to "${binding.switchSetAsReminder.isChecked}".toBoolean(),
                    HAS_COUNTDOWN to "${binding.switchSetAsCountdown.isChecked}".toBoolean(),
                    FROM_GOOGLE to "${binding.switchSetAsGoogle.isChecked}".toBoolean(),
                    LOCATION to "${binding.locationInput.text}".trim(),
                    REMINDERS_DATE to timeFormat2SqlTimestamp(DATE_TIME_FORMAT, remindDate)
                )

                if (binding.switchSetAsGoogle.isChecked) {
                    val googleBeginDate = timeFormat2FirebaseTimestamp(DATE_WEEK_TIME_FORMAT, beginDate)
                    val googleEndDate = timeFormat2FirebaseTimestamp(DATE_WEEK_TIME_FORMAT, endDate)
                    val googleTitle = "${binding.eventTitleInput.text}".trim()
                    val googleNote = "${binding.noteInput.text}".trim()

                    if (ContextCompat.checkSelfPermission(
                            MyApplication.instance,
                            Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            (activity as MainActivity),
                            arrayOf(Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR), 1
                        )
                    } else {

                        viewModel.writeEventIntoGoogleCalendar(
                            googleBeginDate, googleEndDate, googleNote, googleTitle,
                            item, countdown, reminders
                        )
                    }

                } else {
                    viewModel.writeItem(item, countdown, reminders)
                }

            }
        }

        viewModel.isUpdateCompleted.observe(this, androidx.lifecycle.Observer { it ->
            it?.let {
                view?.let { view ->
                    Snackbar.make(view, getString(R.string.save_message), Snackbar.LENGTH_SHORT).show()
                }

                findNavController().navigate(NavigationDirections.actionGlobalCalendarMonthFragment())
            }
        })

        viewModel.isClicked.observe(this, androidx.lifecycle.Observer {
            it?.let {
                binding.saveText.isClickable = false
            }
        })

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EVALUATE) {

            binding.repeatChoose.text = data?.getStringExtra(RESPONSE_EVALUATE)
            Intent().putExtra(RESPONSE, data?.getStringExtra(RESPONSE_EVALUATE))
            activity?.setResult(Activity.RESULT_OK, Intent())
        } else {

            Logger.d("don't have intent")
        }
    }

    private fun setDefaultDate() {
        binding.beginDate.text = setDefaultTime(DATE_WEEK_FORMAT)
        binding.endDate.text = setDefaultTime(DATE_WEEK_FORMAT)
        binding.remindersDateInput.text = setDefaultTime(SIMPLE_DATE_FORMAT)
        binding.beginTime.text = setDefaultTime(TIME_FORMAT)
        binding.endTime.text = setDefaultTime(TIME_FORMAT)
        binding.remindersTimeInput.text = setDefaultTime(TIME_FORMAT)
    }

    private fun showDatePicker(inputDate: TextView) {
        context?.let {
            DatePickerDialog(
                it, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener {
                        _,year, monthOfYear, dayOfMonth ->
                    inputDate.text = timeFormat2String4DatePicker(SIMPLE_DATE_FORMAT, year, monthOfYear, dayOfMonth)
                }, year, monthOfYear, dayOfMonth
            ).show()
        }
    }

    private fun showDatePickerInWeekFormat(inputDate: TextView) {
        context?.let {
            DatePickerDialog(
                it, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener {
                        _,year, monthOfYear, dayOfMonth ->
                    inputDate.text = timeFormat2String4DatePicker(DATE_WEEK_FORMAT, year, monthOfYear, dayOfMonth)
                }, year, monthOfYear, dayOfMonth
            ).show()
        }
    }

    private fun showTimePicker(inputTime: TextView) {
        context?.let {
            TimePickerDialog(
                it, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
                { _, hour, minute ->
                    inputTime.text = timeFormat2String4TimePicker(TIME_FORMAT, hour, minute)
                }, hour, minute, false
            ).show()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            (activity as MainActivity),
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1
        )
    }
}


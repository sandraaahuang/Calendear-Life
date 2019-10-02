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
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.Const
import com.sandra.calendearlife.constant.Const.Companion.EVALUATE_DIALOG
import com.sandra.calendearlife.constant.Const.Companion.REQUEST_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE
import com.sandra.calendearlife.constant.Const.Companion.RESPONSE_EVALUATE
import com.sandra.calendearlife.constant.Const.Companion.value
import com.sandra.calendearlife.constant.DateFormat.Companion.BEGINTIME
import com.sandra.calendearlife.constant.DateFormat.Companion.ENDTIME
import com.sandra.calendearlife.constant.DateFormat.Companion.dayOfMonth
import com.sandra.calendearlife.constant.DateFormat.Companion.hour
import com.sandra.calendearlife.constant.DateFormat.Companion.minute
import com.sandra.calendearlife.constant.DateFormat.Companion.monthOfYear
import com.sandra.calendearlife.constant.DateFormat.Companion.year
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ENDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FREQUENCY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROMGOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASCOUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASREMINDERS
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ISALLDAY
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ISCHECKED
import com.sandra.calendearlife.constant.FirebaseKey.Companion.LOCATION
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.REMINDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETREMINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.databinding.FragmentCalendarEventBinding
import com.sandra.calendearlife.dialog.ChooseFrequencyDialog
import com.sandra.calendearlife.dialog.DiscardDialog
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class CalendarEventFragment : Fragment() {

    private val locale: Locale =
        if (Locale.getDefault().toString() == CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }
    private val timeFormat = SimpleDateFormat("hh:mm a", locale)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", locale)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)
    private val dateWeekFormat = SimpleDateFormat("yyyy/MM/dd EEEE", locale)
    private val dateWeekTimeFormat = SimpleDateFormat("yyyy/MM/dd EEEE hh:mm a", locale)

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
            dialog.setTargetFragment(this, REQUEST_EVALUATE);
            dialog.show(fragmentManager!!, EVALUATE_DIALOG)
        }

        setDefaultDate()

        viewModel.showDateWeekPicker.observe(this, androidx.lifecycle.Observer {
            it?.let {
                showDatePickerInWeekFormat(it)
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

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DiscardDialog().show(this@CalendarEventFragment.fragmentManager!!, "show")
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(fragmentManager!!, "bottom")
        }

        binding.saveText.setOnClickListener {
            // error handle
            if ("${binding.eventTitleInput.text}" == "") {
                binding.eventTitleInput.setHintTextColor(resources.getColor(R.color.delete_red))
            } else {
                val date: String
                val beginDate: String
                val endDate: String

                if (binding.allDaySwitch.isChecked) {
                    date = "${binding.beginDate.text}"
                    beginDate = "${binding.beginDate.text} $BEGINTIME ${getString(R.string.am)}"
                    endDate = "${binding.beginDate.text} $ENDTIME ${getString(R.string.pm)}"
                } else {
                    date = "${binding.beginDate.text}"
                    beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                    endDate = "${binding.endDate.text} ${binding.endTime.text}"
                }

                val remindDate = "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"

                val countdown = hashMapOf(
                    SETDATE to FieldValue.serverTimestamp(),
                    TITLE to "${binding.eventTitleInput.text}".trim(),
                    NOTE to "${binding.noteInput.text}".trim(),
                    TARGETDATE to Timestamp(dateWeekTimeFormat.parse(endDate).time),
                    OVERDUE to false
                )

                val reminders = hashMapOf(
                    SETDATE to FieldValue.serverTimestamp(),
                    TITLE to "${binding.eventTitleInput.text}".trim(),
                    SETREMINDATE to true,
                    REMINDDATE to Timestamp(dateTimeFormat.parse(remindDate).time),
                    ISCHECKED to false,
                    NOTE to "${binding.noteInput.text}".trim(),
                    FREQUENCY to value
                )

                val item = hashMapOf(
                    FREQUENCY to value,
                    DATE to Timestamp(dateWeekFormat.parse(date).time),
                    SETDATE to FieldValue.serverTimestamp(),
                    BEGINDATE to Timestamp(dateWeekTimeFormat.parse(beginDate).time),
                    ENDDATE to Timestamp(dateWeekTimeFormat.parse(endDate).time),
                    TITLE to "${binding.eventTitleInput.text}".trim(),
                    NOTE to "${binding.noteInput.text}".trim(),
                    ISALLDAY to "${binding.allDaySwitch.isChecked}",
                    HASREMINDERS to "${binding.switchSetAsReminder.isChecked}".toBoolean(),
                    HASCOUNTDOWN to "${binding.switchSetAsCountdown.isChecked}".toBoolean(),
                    FROMGOOGLE to "${binding.switchSetAsGoogle.isChecked}".toBoolean(),
                    LOCATION to "${binding.locationInput.text}".trim()
                )

                if (binding.switchSetAsGoogle.isChecked) {
                    val gBeginDate = com.google.firebase.Timestamp(dateWeekTimeFormat.parse(beginDate))
                    val gEndDate = com.google.firebase.Timestamp(dateWeekTimeFormat.parse(endDate))
                    val gTitle = "${binding.eventTitleInput.text}".trim()
                    val gNote = "${binding.noteInput.text}".trim()

                    if (ContextCompat.checkSelfPermission(
                            this.context!!,
                            Manifest.permission.READ_CALENDAR
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            (activity as MainActivity),
                            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1
                        )
                    } else {

                        viewModel.writeGoogle(
                            gBeginDate, gEndDate, gNote, gTitle,
                            item, countdown, reminders
                        )
                    }

                } else {
                    viewModel.writeItem(item, countdown, reminders)
                }

            }
        }

        viewModel.isUpdateCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                Snackbar.make(this.view!!, getString(R.string.save_message), Snackbar.LENGTH_SHORT).show()
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
            val evaluate = data?.getStringExtra(RESPONSE_EVALUATE)

            binding.repeatChoose.text = evaluate
            val intent = Intent()
            intent.putExtra(RESPONSE, evaluate);
            activity?.setResult(Activity.RESULT_OK, intent)
        }
    }

    private fun setDefaultDate() {
        binding.beginDate.text = dateWeekFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))
        binding.endDate.text = dateWeekFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))
        binding.remindersDateInput.text =
            simpleDateFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))
        binding.beginTime.text = timeFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))
        binding.endTime.text = timeFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))
        binding.remindersTimeInput.text = timeFormat.format(Date(com.google.firebase.Timestamp.now().seconds * 1000))
    }

    private fun showDatePicker(inputDate: TextView) {
        val datePickerDialog = DatePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
            { _, year, monthOfYear, dayOfMonth ->
                inputDate.text =
                    simpleDateFormat.format(Date(year - 1900, monthOfYear, dayOfMonth))
            }, year, monthOfYear, dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showDatePickerInWeekFormat(inputDate: TextView) {
        val datePickerDialog = DatePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
            { _, year, monthOfYear, dayOfMonth ->
                inputDate.text =
                    dateWeekFormat.format(Date(year - 1900, monthOfYear, dayOfMonth))
            }, year, monthOfYear, dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showTimePicker(inputTime: TextView) {
        val timePickerDialog = TimePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { _, hour, minute ->
                inputTime.text =
                    timeFormat.format(Date(year - 1900, monthOfYear, dayOfMonth, hour, minute))
            }, hour, minute, false
        )
        timePickerDialog.show()
    }

}


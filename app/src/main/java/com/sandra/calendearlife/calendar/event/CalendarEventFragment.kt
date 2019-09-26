package com.sandra.calendearlife.calendar.event

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.databinding.CalendarEventFragmentBinding
import com.sandra.calendearlife.dialog.RepeatDialog
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import android.provider.CalendarContract.Calendars
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Handler
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.MainActivity
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.util.UserManager


class CalendarEventFragment : Fragment() {

    // get dialog result data
    var RESPONSE = "response"
    var EVALUATE_DIALOG = "evaluate_dialog"
    var REQUEST_EVALUATE = 0X110

    val locale =
        if (Locale.getDefault().toString() == "zh-rtw") {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }

    var db = FirebaseFirestore.getInstance()
    private val dateWeekFormat = SimpleDateFormat("yyyy/MM/dd EEEE", locale)
    private val timeFormat = SimpleDateFormat("hh:mm a", locale)
    private val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd", locale)
    private val dateWeekTimeFormat = SimpleDateFormat("yyyy/MM/dd EEEE hh:mm a", locale)
    private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", locale)

    lateinit var binding: CalendarEventFragmentBinding

    private val viewModel: CalendarEventViewModel by lazy{
        ViewModelProviders.of(this).get(CalendarEventViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = CalendarEventFragmentBinding.inflate(inflater, container, false)

        binding.allDaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.endDate.visibility = View.GONE
                binding.beginTime.visibility = View.GONE
                binding.endTime.visibility = View.GONE
            }
            else {
                binding.endDate.visibility = View.VISIBLE
                binding.beginTime.visibility = View.VISIBLE
                binding.endTime.visibility = View.VISIBLE
            }
        }

        binding.switchSetAsReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.setRemindLayout.visibility = View.VISIBLE
            }
            else {
                binding.setRemindLayout.visibility = View.GONE
            }
        }

        binding.repeatChoose.setOnClickListener {
            val dialog = RepeatDialog()
            //setTargetFragment
            dialog.setTargetFragment(this, REQUEST_EVALUATE);
            dialog.show(fragmentManager!!, EVALUATE_DIALOG)
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val monthOfYear = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        binding.beginDate.text = dateWeekFormat.format(Date(com.google.firebase.Timestamp.now().seconds*1000))
        binding.endDate.text = dateWeekFormat.format(Date(com.google.firebase.Timestamp.now().seconds*1000))
        binding.remindersDateInput.text = simpleDateFormat.format(Date(com.google.firebase.Timestamp.now().seconds*1000))
        binding.beginTime.text = timeFormat.format(Date(com.google.firebase.Timestamp.now().seconds*1000))
        binding.endTime.text = timeFormat.format(Date(com.google.firebase.Timestamp.now().seconds*1000))
        binding.remindersTimeInput.text = timeFormat.format(Date(com.google.firebase.Timestamp.now().seconds*1000))

        binding.beginDate.setOnClickListener {

            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = dateWeekFormat.format(date)
                    binding.beginDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )

            datePickerDialog.show()
        }

        binding.endDate.setOnClickListener {
            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = dateWeekFormat.format(date)
                    binding.endDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.remindersDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)


            val datePickerDialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    // Display Selected setDate in textbox
                    val date = Date(year -1900, monthOfYear, dayOfMonth, hour, minute)
                    val stringTime = simpleDateFormat.format(date)
                    binding.remindersDateInput.text=
                        "$stringTime" }, year, monthOfYear, dayOfMonth
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
            TimePickerDialog(it.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year, monthOfYear, dayOfMonth, hour, minute)
                val stringTime = timeFormat.format(date)
                binding.remindersTimeInput.text =
                    "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.beginTime.setOnClickListener {
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = timeFormat.format(date)
                binding.beginTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.endTime.setOnClickListener {
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = timeFormat.format(date)
                binding.endTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

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

                if (binding.allDaySwitch.isChecked){
                    date = "${binding.beginDate.text}"
                    beginDate = "${binding.beginDate.text} 00:01 ${getString(R.string.am)}"
                    endDate = "${binding.beginDate.text} 11:59 ${getString(R.string.pm)}"
                } else {
                    date = "${binding.beginDate.text}"
                    beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                    endDate = "${binding.endDate.text} ${binding.endTime.text}"
                }

                val remindDate = "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"

                val countdown = hashMapOf(
                    "setDate" to FieldValue.serverTimestamp(),
                    "title" to "${binding.eventTitleInput.text}",
                    "note" to "${binding.noteInput.text}",
                    "targetDate" to Timestamp(dateWeekTimeFormat.parse(endDate).time),
                    "overdue" to false)

                val reminders = hashMapOf(
                    "setDate" to FieldValue.serverTimestamp(),
                    "title" to "${binding.eventTitleInput.text}",
                    "setRemindDate" to true,
                    "remindDate" to Timestamp(dateTimeFormat.parse(remindDate).time),
                    "isChecked" to false,
                    "note" to "${binding.noteInput.text}",
                    "frequency" to RepeatDialog.value)

                val item = hashMapOf(
                    "frequency" to RepeatDialog.value,
                    "date" to Timestamp(simpleDateFormat.parse(date).time),
                    "setDate" to FieldValue.serverTimestamp(),
                    "beginDate" to Timestamp(dateWeekTimeFormat.parse(beginDate).time),
                    "endDate" to Timestamp(dateWeekTimeFormat.parse(endDate).time),
                    "title" to "${binding.eventTitleInput.text}",
                    "note" to "${binding.noteInput.text}",
                    "isAllDay" to "${binding.allDaySwitch.isChecked}",
                    "hasReminders" to "${binding.switchSetAsReminder.isChecked}".toBoolean(),
                    "hasCountdown" to "${binding.switchSetAsCountdown.isChecked}".toBoolean(),
                    "fromGoogle" to "${binding.switchSetAsGoogle.isChecked}".toBoolean(),
                    "location" to "${binding.locationInput.text}"
                )

                if (binding.switchSetAsGoogle.isChecked){
                    val gBeginDate = com.google.firebase.Timestamp(dateWeekTimeFormat.parse(beginDate))
                    val gEndDate = com.google.firebase.Timestamp(dateWeekTimeFormat.parse(endDate))
                    val gTitle = "${binding.eventTitleInput.text}"
                    val gNote = "${binding.noteInput.text}"

                    if (ContextCompat.checkSelfPermission(this.context!!,
                            Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions((activity as MainActivity),
                            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR), 1)
                    } else {

                        viewModel.writeGoogle(gBeginDate, gEndDate, gNote, gTitle,
                            item, countdown, reminders)
                    }

                }

                 else {
                    viewModel.writeItem(item, countdown, reminders)
                    Log.d("sandraaa", "fail")
                }

            }
        }

        viewModel.updateCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                Snackbar.make(this.view!!, getString(R.string.save_message), Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(NavigationDirections.actionGlobalCalendarMonthFragment())
            }
        })

        viewModel.clicked.observe(this, androidx.lifecycle.Observer {
            it?.let {
                binding.saveText.isClickable = false
            }
        })
        
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EVALUATE)
        {
            val evaluate = data?.getStringExtra(RepeatDialog().RESPONSE_EVALUATE2)

            binding.repeatChoose.text = evaluate
            val intent = Intent()
            intent.putExtra(RESPONSE, evaluate);
            activity?.setResult(Activity.RESULT_OK, intent)
        }
    }



}


package com.sandra.calendearlife.calendar.detail

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.CalendarDetailFragmentBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class CalendarDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val dateWeekFormat = SimpleDateFormat("yyyy/MM/dd EEEE")
        val timeFormat = SimpleDateFormat("hh:mm a")
        val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a")
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

        val binding = CalendarDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(activity).application
        val calendar = CalendarDetailFragmentArgs.fromBundle(arguments!!).calendar
        val viewModelFactory = CalendarDetailFactory(calendar, application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(CalendarDetailViewModel::class.java)
        binding.viewModel = viewModel

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

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val monthOfYear = cal.get(Calendar.MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        binding.beginDate.setOnClickListener {

            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = timeFormat.format(date)
                binding.beginTime.text = "$stringTime" }, hour, minute, false
            ).show()

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

            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = timeFormat.format(date)
                binding.endTime.text = "$stringTime" }, hour, minute, false
            ).show()

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

            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = timeFormat.format(date)
                binding.remindersTimeInput.text = "$stringTime" }, hour, minute, false
            ).show()

            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = dateWeekFormat.format(date)
                    binding.remindersDateInput.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )

            datePickerDialog.show()
        }



        binding.deleteButton.setOnClickListener {
            viewModel.deleteItem(calendar.documentID!!)
            Log.d("sandraaa", "delete = ${calendar.documentID}")
            Handler().postDelayed({findNavController().navigate(NavigationDirections.actionGlobalCalendarMonthFragment())}
                ,3000)

        }

        binding.saveButton.setOnClickListener {

            val beginDate: String
            val endDate: String

            if (binding.allDaySwitch.isChecked){
                beginDate = "${binding.beginDate.text} 00:01 AM"
                endDate = "${binding.beginDate.text} 11:59 PM"
            } else {
                beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                endDate = "${binding.endDate.text} ${binding.endTime.text}"
            }

            val remindDate = "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"

            val updateRemind = hashMapOf(
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "remindDate" to Timestamp(dateWeekFormat.parse(remindDate).time)
            )

            val updateCalendar = hashMapOf(
                "date" to Timestamp(simpleDateFormat.parse(beginDate).time),
                "beginDate" to Timestamp(dateWeekFormat.parse(beginDate).time),
                "endDate" to Timestamp(dateWeekFormat.parse(endDate).time),
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "isAllDay" to "${binding.allDaySwitch.isChecked}",
                "hasReminders" to "${binding.switchSetAsReminder.isChecked}".toBoolean(),
                "hasCountdown" to "${binding.switchSetAsCountdown.isChecked}".toBoolean()
            )

            val updateCountdown = hashMapOf(
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "targetDate" to Timestamp(simpleDateFormat.parse(endDate).time)
            )

            Log.d("updateItem", "id = ${calendar.documentID}, " +
                    "updateCalendar = $updateCalendar," +
                    "updateCountdown = $updateCountdown," +
                    "updateRemind = $updateRemind")


//            viewModel.updateItem(calendar.documentID!!,updateCalendar,updateCountdown,updateRemind)
        }

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }

        return binding.root
    }
}
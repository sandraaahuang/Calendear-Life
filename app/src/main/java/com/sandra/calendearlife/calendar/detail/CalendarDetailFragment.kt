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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.CalendarDetailFragmentBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class CalendarDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val timeFormat = SimpleDateFormat("hh:mm a")
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a")
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")

        val binding = CalendarDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(activity).application
        val calendar = CalendarDetailFragmentArgs.fromBundle(arguments!!).calendar
        val viewModelFactory = CalendarDetailFactory(calendar, application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(CalendarDetailViewModel::class.java)
        binding.viewModel = viewModel

        if (calendar.color == "245E2C" ||calendar.color == "8C6B8B" || calendar.color == "542437"
            || calendar.color == "53777A" ||calendar.color == "A6292F"){
            binding.allDayLayout.visibility = View.VISIBLE
            binding.beginDate.visibility = View.VISIBLE


            if (calendar.color == "8C6B8B" || calendar.color == "542437"||calendar.color == "A6292F"){
                if (calendar.isAllDay){
                    binding.beginTime.visibility = View.GONE
                    binding.endDateText.visibility = View.GONE
                    binding.endDate.visibility = View.GONE
                    binding.endTime.visibility = View.GONE
                }
                else {
                    binding.endDate.visibility = View.VISIBLE
                    binding.endTime.visibility = View.VISIBLE
                }
            }

        } else {
            binding.allDayLayout.visibility = View.GONE
        }



        if (calendar.color == "C02942" || calendar.color == "542437" || calendar.color == "A6292F" ||calendar.hasReminders){
            binding.remindLayout.visibility = View.VISIBLE
        } else {
            binding.remindLayout.visibility = View.GONE
        }

        if (calendar.color == "100038" ||calendar.color == "53777A" ||calendar.color == "A6292F" ||calendar.hasCountdown) {
            binding.countdownLayout.visibility = View.VISIBLE
        } else {
            binding.countdownLayout.visibility = View.GONE
        }

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val monthOfYear = cal.get(Calendar.MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        binding.beginDate.setOnClickListener {

            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.beginDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )

            datePickerDialog.show()
        }

        binding.beginTime.setOnClickListener {
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = timeFormat.format(date)
                binding.beginTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.endDate.setOnClickListener {

            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.endDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )

            datePickerDialog.show()
        }

        binding.endTime.setOnClickListener {
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = timeFormat.format(date)
                binding.endTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.remindDate.setOnClickListener {

            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.remindDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )

            datePickerDialog.show()
        }

        binding.remindTime.setOnClickListener {
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = timeFormat.format(date)
                binding.remindTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.targetDateInput.setOnClickListener {

            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.beginDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )

            datePickerDialog.show()
        }

        binding.deleteButton.setOnClickListener {
            viewModel.deleteItem(calendar.documentID!!)
            Log.d("sandraaa", "delete = ${calendar.documentID}")

            if (calendar.fromGoogle){

                viewModel.deleteEvent(calendar.documentID)
            } else {
                Log.d("sandraaa", "is not google item")
            }

            Snackbar.make(this.view!!, getString(R.string.delete_message), Snackbar.LENGTH_LONG).show()
            Handler().postDelayed({findNavController().navigate(NavigationDirections
                .actionGlobalCalendarMonthFragment())}, 3000)

        }

        binding.saveButton.setOnClickListener {

            val beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
            val endDate = "${binding.endDate.text} ${binding.endTime.text}"
            val tatgetDate = "${binding.endDate.text}"
            val remindDate = "${binding.remindDate.text} ${binding.remindTime.text}"


            val updateRemind = hashMapOf(
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "remindDate" to Timestamp(dateTimeFormat.parse(remindDate).time)
            )

            val updateCalendar = hashMapOf(
                "date" to Timestamp(simpleDateFormat.parse(beginDate).time),
                "beginDate" to Timestamp(dateTimeFormat.parse(beginDate).time),
                "endDate" to Timestamp(dateTimeFormat.parse(endDate).time),
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "location" to "${binding.locationInput.text}"
            )

            val updateCountdown = hashMapOf(
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "targetDate" to Timestamp(simpleDateFormat.parse(tatgetDate).time)
            )

            viewModel.updateItem(calendar.documentID!!,updateCalendar,updateCountdown,updateRemind)

            if (calendar.fromGoogle){
            viewModel.updateEvent(calendar.documentID,
                "${binding.detailTitleInput.text}",
                "${binding.noteInput.text}",
                com.google.firebase.Timestamp(dateTimeFormat.parse(beginDate)),
                com.google.firebase.Timestamp(dateTimeFormat.parse(endDate)))
            } else {
                Log.d("sandraaa", "is not google item")
            }

            Snackbar.make(this.view!!, getString(R.string.update_message), Snackbar.LENGTH_LONG).show()
            Handler().postDelayed({findNavController().navigate(NavigationDirections
                .actionGlobalCalendarMonthFragment())}, 3000)
        }



        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }

        return binding.root
    }
}
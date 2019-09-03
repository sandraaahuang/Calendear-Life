package com.sandra.calendearlife.calendar

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.databinding.CalendarEventFragmentBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarEventFragment : Fragment() {

    lateinit var binding: CalendarEventFragmentBinding

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

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val monthOfYear = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val transferDay = when (dayOfWeek){
            2 -> "Monday"
            3 -> "Tuesday"
            4 -> "Wednesday"
            5 -> "Thursday"
            6 -> "Friday"
            7 -> "Saturday"
            else -> "Sunday"
        }

        val chooseDate = "$transferDay/${monthOfYear + 1 }/$dayOfMonth"

        binding.beginDate.text = chooseDate
        binding.endDate.text = chooseDate

        binding.beginDate.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("EEEE MM/dd")
            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.beginDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )

            datePickerDialog.show()
        }

        binding.endDate.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("EEEE MM/dd")
            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.endDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.remindersInput.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.remindersInput.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.beginTime.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("h:mm a")
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = simpleDateFormat.format(date)
                binding.beginTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.endTime.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("h:mm a")
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = simpleDateFormat.format(date)
                binding.endTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(fragmentManager!!, "bottom")
        }
        
        return binding.root
    }

}


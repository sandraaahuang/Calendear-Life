package com.sandra.calendearlife.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sandra.calendearlife.databinding.RemindersDetailFragmentBinding
import java.text.SimpleDateFormat
import java.util.*


class RemindersDetailFragment : Fragment() {

    private lateinit var binding: RemindersDetailFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = RemindersDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.switchRemindDay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.remindLayout.visibility = View.VISIBLE
            }
            else {
                binding.remindLayout.visibility = View.GONE
            }
        }

        binding.remindLayout.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)


            TimePickerDialog(it.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year, monthOfYear, dayOfMonth, hour, minute)
                val stringTime = SimpleDateFormat("h:mm a").format(date)
                binding.remindTime.text =
                    "$stringTime" }, hour, minute, false
            ).show()

            val datePickerDialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth, hour, minute)
                    val stringDate = SimpleDateFormat("yyyy/MM/dd").format(date)
                    // Display Selected setDate in textbox
                    binding.remindDate.text=
                        "$stringDate" }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        return binding.root
    }
}




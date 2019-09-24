package com.sandra.calendearlife.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.RemindersDetailFragmentBinding
import java.text.SimpleDateFormat
import java.util.*


class RemindersDetailFragment : Fragment() {

    private lateinit var binding: RemindersDetailFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = RemindersDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(activity).application
        val reminders = RemindersDetailFragmentArgs.fromBundle(arguments!!).remindersProperty
        val viewModelFactory = DetailViewModelFactory(reminders, application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(RemindersDetailViewModel::class.java)
        binding.viewModel = viewModel

        binding.switchRemindDay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.remindLayout.visibility = View.VISIBLE
            }
            else {
                binding.remindLayout.visibility = View.GONE
            }
        }

        binding.remindDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = SimpleDateFormat("yyyy/MM/dd").format(date)
                    // Display Selected setDate in textbox
                    binding.remindDate.text=
                        "$stringDate" }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.remindTime.setOnClickListener {
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
        }

        binding.saveButton2.setOnClickListener {
            val remindDate = "${binding.remindDate.text} ${binding.remindTime.text}"
            val putInDate = Date(remindDate)

            val updateItem = hashMapOf(
                "title" to "${binding.remindersTitle.text}",
                "note" to "${binding.editTextRemindNote.text}",
                "remindDate" to java.sql.Timestamp(putInDate.time)
            )

            val calendarItem = hashMapOf(
                "title" to "${binding.remindersTitle.text}",
                "note" to "${binding.editTextRemindNote.text}",
                "beginDate" to java.sql.Timestamp(putInDate.time),
                "date" to java.sql.Timestamp(putInDate.time)
            )

            viewModel.updateItem(updateItem, calendarItem, reminders.documentID)

            Snackbar.make(this.view!!, getString(R.string.update_message), Snackbar.LENGTH_LONG).show()
        }

        binding.deleteButton2.setOnClickListener {
            viewModel.deleteItem(reminders.documentID)
            Snackbar.make(this.view!!, getString(R.string.delete_message), Snackbar.LENGTH_LONG).show()
        }

        viewModel.updateCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                findNavController().navigate(NavigationDirections
                    .actionGlobalHomeFragment())
            }
        })

        return binding.root
    }
}




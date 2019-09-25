package com.sandra.calendearlife.countdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.CountdownDetailFragmentBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.reminders.DetailViewModelFactory
import com.sandra.calendearlife.reminders.RemindersDetailFragmentArgs
import com.sandra.calendearlife.reminders.RemindersDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

class CountdownDetailFragment : Fragment() {

    private lateinit var binding: CountdownDetailFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = CountdownDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(activity).application

        val countdown = CountdownDetailFragmentArgs.fromBundle(arguments!!).countdownProperty
        val viewModelFactory = CountdownDetailFactory(countdown, application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(CountdownDetailViewModel::class.java)
        binding.viewModel = viewModel

        binding.remindLayout.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.targetDateInput.text=
                        "$stringDate" }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.saveButton.setOnClickListener {
            val targetDate = binding.targetDateInput.text.toString()
            val putInDate = Date(targetDate)

            val updateItem = hashMapOf(
                "title" to "${binding.editTextCountdown.text}",
                "note" to "${binding.editTextCountdownNote.text}",
                "targetDate" to java.sql.Timestamp(putInDate.time)
            )

            val calendarItem = hashMapOf(
                "title" to "${binding.editTextCountdown.text}",
                "note" to "${binding.editTextCountdownNote.text}",
                "beginDate" to java.sql.Timestamp(putInDate.time),
                "date" to java.sql.Timestamp(putInDate.time)
            )

            viewModel.updateItem(updateItem,calendarItem, countdown.documentID)
            Snackbar.make(this.view!!, getString(R.string.update_message), Snackbar.LENGTH_LONG).show()
        }

        binding.deleteButton.setOnClickListener {

            viewModel.deleteItem(countdown.documentID)
            Snackbar.make(this.view!!, getString(R.string.delete_message), Snackbar.LENGTH_LONG).show()
        }
        viewModel.updateCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                findNavController().navigate(NavigationDirections
                    .actionGlobalHomeFragment())
            }
        })

        viewModel.clicked.observe(this, androidx.lifecycle.Observer {
            it?.let {
                binding.saveButton.isClickable = false
                binding.deleteButton.isClickable = false
            }
        })

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DiscardDialog().show(this@CountdownDetailFragment.fragmentManager!!, "show")
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }

        return binding.root
    }
}




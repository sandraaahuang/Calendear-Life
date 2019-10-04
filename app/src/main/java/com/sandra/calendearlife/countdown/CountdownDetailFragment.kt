package com.sandra.calendearlife.countdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.Const.Companion.SHOW
import com.sandra.calendearlife.constant.DateFormat.Companion.dayOfMonth
import com.sandra.calendearlife.constant.DateFormat.Companion.monthOfYear
import com.sandra.calendearlife.constant.DateFormat.Companion.year
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGIN_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SIMPLE_DATE_FORMAT
import com.sandra.calendearlife.constant.timeFormat2SqlTimestamp
import com.sandra.calendearlife.constant.timeFormat2String4DatePicker
import com.sandra.calendearlife.databinding.FragmentCountdownDetailBinding
import com.sandra.calendearlife.dialog.DiscardDialog

class CountdownDetailFragment : Fragment() {

    private lateinit var binding: FragmentCountdownDetailBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCountdownDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(activity).application

        val countdown = CountdownDetailFragmentArgs.fromBundle(arguments!!).countdownProperty
        val viewModelFactory = CountdownDetailFactory(countdown, application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory
        ).get(CountdownDetailViewModel::class.java)
        binding.viewModel = viewModel

        binding.saveButton.setOnClickListener {
            val targetDate = binding.targetDateInput.text.toString()

            val updateItem = hashMapOf(
                TITLE to "${binding.editTextCountdown.text}".trim(),
                NOTE to "${binding.editTextCountdownNote.text}".trim(),
                TARGET_DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate)
            )

            val calendarItem = hashMapOf(
                TITLE to "${binding.editTextCountdown.text}".trim(),
                NOTE to "${binding.editTextCountdownNote.text}".trim(),
                BEGIN_DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate),
                DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate)
            )

            viewModel.updateItem(updateItem, calendarItem, countdown.documentID)
            Snackbar.make(this.view!!, getString(R.string.update_message), Snackbar.LENGTH_LONG).show()
        }

        binding.deleteButton.setOnClickListener {

            viewModel.deleteItem(countdown.documentID)
            Snackbar.make(this.view!!, getString(R.string.delete_message), Snackbar.LENGTH_LONG).show()
        }
        viewModel.isUpdateCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                findNavController().navigate(
                    NavigationDirections
                        .actionGlobalHomeFragment()
                )
            }
        })

        viewModel.isClicked.observe(this, androidx.lifecycle.Observer {
            it?.let {
                binding.saveButton.isClickable = false
                binding.deleteButton.isClickable = false
            }
        })

        viewModel.showDatePicker.observe(this, androidx.lifecycle.Observer { clickedDate ->
            clickedDate?.let {
                showDatePicker(it)
            }
        })

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DiscardDialog().show(this@CountdownDetailFragment.fragmentManager!!, SHOW)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, SHOW)
        }

        return binding.root
    }

    private fun showDatePicker(inputDate: TextView) {
        DatePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
            { _, year, monthOfYear, dayOfMonth ->
                inputDate.text =
                    timeFormat2String4DatePicker(SIMPLE_DATE_FORMAT, year, monthOfYear, dayOfMonth)
            }, year, monthOfYear, dayOfMonth
        ).show()
    }
}




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
import com.sandra.calendearlife.constant.Const
import com.sandra.calendearlife.constant.DateFormat.Companion.dayOfMonth
import com.sandra.calendearlife.constant.DateFormat.Companion.monthOfYear
import com.sandra.calendearlife.constant.DateFormat.Companion.year
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.databinding.FragmentCountdownDetailBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import java.text.SimpleDateFormat
import java.util.*

class CountdownDetailFragment : Fragment() {

    private lateinit var binding: FragmentCountdownDetailBinding

    private val locale: Locale =
        if (Locale.getDefault().toString() == CHINESE) {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

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
                TARGETDATE to java.sql.Timestamp(simpleDateFormat.parse(targetDate).time)
            )

            val calendarItem = hashMapOf(
                TITLE to "${binding.editTextCountdown.text}".trim(),
                NOTE to "${binding.editTextCountdownNote.text}".trim(),
                BEGINDATE to java.sql.Timestamp(simpleDateFormat.parse(targetDate).time),
                DATE to java.sql.Timestamp(simpleDateFormat.parse(targetDate).time)
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

        viewModel.showDatePicker.observe(this, androidx.lifecycle.Observer {
            it?.let {
                showDatePicker(it)
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
}




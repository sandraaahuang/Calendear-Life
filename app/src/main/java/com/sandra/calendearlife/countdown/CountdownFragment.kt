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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.Const.Companion.SHOW
import com.sandra.calendearlife.constant.DateFormat.Companion.dayOfMonth
import com.sandra.calendearlife.constant.DateFormat.Companion.monthOfYear
import com.sandra.calendearlife.constant.DateFormat.Companion.year
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGIN_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.END_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROM_GOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HAS_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGET_DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.constant.SIMPLE_DATE_FORMAT
import com.sandra.calendearlife.constant.SharedPreferenceKey.Companion.CHINESE
import com.sandra.calendearlife.constant.setDefaultTime
import com.sandra.calendearlife.constant.timeFormat2SqlTimestamp
import com.sandra.calendearlife.constant.timeFormat2String4DatePicker
import com.sandra.calendearlife.databinding.FragmentCountdownBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import java.text.SimpleDateFormat
import java.util.*


class CountdownFragment : Fragment() {

    private val viewModel: CountdownViewModel by lazy {
        ViewModelProviders.of(this).get(CountdownViewModel::class.java)
    }

    private lateinit var binding: FragmentCountdownBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCountdownBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.countdownDateInput.text = setDefaultTime(SIMPLE_DATE_FORMAT)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DiscardDialog().show(this@CountdownFragment.fragmentManager!!, SHOW)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, SHOW)
        }

        binding.saveText.setOnClickListener {

            if ("${binding.countdownTitleInput.text}" == "") {
                binding.countdownTitleInput.setHintTextColor(resources.getColor(R.color.delete_red))
            } else {

                val targetDate = binding.countdownDateInput.text.toString()

                val calendar = hashMapOf(
                    COLOR to COLOR_COUNTDOWN,
                    SET_DATE to FieldValue.serverTimestamp(),
                    BEGIN_DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate),
                    END_DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate),
                    DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate),
                    TITLE to "${binding.countdownTitleInput.text}".trim(),
                    NOTE to "${binding.noteInput.text}".trim(),
                    HAS_COUNTDOWN to true,
                    FROM_GOOGLE to false
                )

                val countdown = hashMapOf(
                    SET_DATE to FieldValue.serverTimestamp(),
                    TITLE to "${binding.countdownTitleInput.text}".trim(),
                    NOTE to "${binding.noteInput.text}".trim(),
                    TARGET_DATE to timeFormat2SqlTimestamp(SIMPLE_DATE_FORMAT, targetDate),
                    OVERDUE to false
                )

                viewModel.writeItem(calendar, countdown)
                Snackbar.make(this.view!!, getString(R.string.save_message), Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isUpdateCompleted.observe(this, androidx.lifecycle.Observer {
            it?.let {
                findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
            }
        })

        viewModel.isClicked.observe(this, androidx.lifecycle.Observer {
            it?.let {
                binding.saveText.isClickable = false
            }
        })

        viewModel.showDatePicker.observe(this, androidx.lifecycle.Observer { clickedDate ->
            clickedDate?.let {
                showDatePicker(it)
            }
        })

        return binding.root
    }

    private fun showDatePicker(inputDate: TextView) {
        val datePickerDialog = DatePickerDialog(
            this.context!!, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
            { _, year, monthOfYear, dayOfMonth ->
                inputDate.text = timeFormat2String4DatePicker(SIMPLE_DATE_FORMAT, year, monthOfYear, dayOfMonth)
            }, year, monthOfYear, dayOfMonth
        )
        datePickerDialog.show()
    }
}

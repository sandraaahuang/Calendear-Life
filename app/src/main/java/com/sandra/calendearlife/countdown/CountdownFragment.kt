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
import com.sandra.calendearlife.constant.DateFormat.Companion.dayOfMonth
import com.sandra.calendearlife.constant.DateFormat.Companion.monthOfYear
import com.sandra.calendearlife.constant.DateFormat.Companion.year
import com.sandra.calendearlife.constant.FirebaseKey.Companion.BEGINDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATA
import com.sandra.calendearlife.constant.FirebaseKey.Companion.DATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.ENDDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.FROMGOOGLE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.HASCOUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.NOTE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.OVERDUE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.SETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TARGETDATE
import com.sandra.calendearlife.constant.FirebaseKey.Companion.TITLE
import com.sandra.calendearlife.databinding.FragmentCountdownBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import java.text.SimpleDateFormat
import java.util.*


class CountdownFragment : Fragment() {

    val locale: Locale =
        if (Locale.getDefault().toString() == "zh-rtw") {
            Locale.TAIWAN
        } else {
            Locale.ENGLISH
        }
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", locale)

    private val viewModel: CountdownViewModel by lazy {
        ViewModelProviders.of(this).get(CountdownViewModel::class.java)
    }

    private lateinit var binding: FragmentCountdownBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCountdownBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.countdownDateInput.text = simpleDateFormat.format(Date(Timestamp.now().seconds*1000))

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                DiscardDialog().show(this@CountdownFragment.fragmentManager!!, "show")
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }

        binding.saveText.setOnClickListener {

            if ("${binding.countdownTitleInput.text}" == "") {
                binding.countdownTitleInput.setHintTextColor(resources.getColor(R.color.delete_red))
            } else {

                val targetDate = binding.countdownDateInput.text.toString()

                val calendar = hashMapOf(
                    COLOR to COLOR_COUNTDOWN,
                    SETDATE to FieldValue.serverTimestamp(),
                    BEGINDATE to java.sql.Timestamp(simpleDateFormat.parse(targetDate).time),
                    ENDDATE to java.sql.Timestamp(simpleDateFormat.parse(targetDate).time),
                    DATE to java.sql.Timestamp(simpleDateFormat.parse(targetDate).time),
                    TITLE to "${binding.countdownTitleInput.text}".trim(),
                    NOTE to "${binding.noteInput.text}".trim(),
                    HASCOUNTDOWN to true,
                    FROMGOOGLE to false
                )

                val countdown = hashMapOf(
                    SETDATE to FieldValue.serverTimestamp(),
                    TITLE to "${binding.countdownTitleInput.text}".trim(),
                    NOTE to "${binding.noteInput.text}".trim(),
                    TARGETDATE to java.sql.Timestamp(simpleDateFormat.parse(targetDate).time),
                    OVERDUE to false
                )

                viewModel.writeItem(calendar,countdown)
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

        viewModel.showDatePicker.observe(this, androidx.lifecycle.Observer {
            it?.let {
                showDatePicker(it)
            }
        })

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

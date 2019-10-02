package com.sandra.calendearlife.countdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.DateFormat.Companion.simpleDateFormat
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

        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)

        binding.countdownDateInput.text = simpleDateFormat.format(Date(Timestamp.now().seconds*1000))

        binding.editCountdownLayout.setOnClickListener {

            val datepickerdialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    // Display Selected setDate in textbox
                    binding.countdownDateInput.text = "$stringDate" }, y, m, d
            )
            datepickerdialog.show()
        }

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

            if (binding.countdownDateInput.text == ""){
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH)
                val d = cal.get(Calendar.DAY_OF_MONTH)

                val datepickerdialog = DatePickerDialog(
                    it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                    { _, year, monthOfYear, dayOfMonth ->
                        val date = Date(year -1900, monthOfYear, dayOfMonth)
                        val stringDate = SimpleDateFormat("yyyy/MM/dd").format(date)
                        // Display Selected setDate in textbox
                        binding.countdownDateInput.text = "$stringDate" }, y, m, d
                )
                datepickerdialog.show()
            } else if ("${binding.countdownTitleInput.text}" == "") {
                binding.countdownTitleInput.setHintTextColor(resources.getColor(R.color.delete_red))
            } else {

                val targetDate = binding.countdownDateInput.text.toString()
                val putInDate = Date(targetDate)

                val calendar = hashMapOf(
                    "color" to "100038",
                    "setDate" to FieldValue.serverTimestamp(),
                    "beginDate" to java.sql.Timestamp(putInDate.time),
                    "endDate" to java.sql.Timestamp(putInDate.time),
                    "date" to java.sql.Timestamp(putInDate.time),
                    "title" to "${binding.countdownTitleInput.text}".trim(),
                    "note" to "${binding.noteInput.text}".trim(),
                    "hasCountdown" to true,
                    "fromGoogle" to false
                )



                val countdown = hashMapOf(
                    "setDate" to FieldValue.serverTimestamp(),
                    "title" to "${binding.countdownTitleInput.text}".trim(),
                    "note" to "${binding.noteInput.text}".trim(),
                    "targetDate" to java.sql.Timestamp(putInDate.time),
                    "overdue" to false
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

        return binding.root
    }
}

package com.sandra.calendearlife.countdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.sandra.calendearlife.databinding.CountdownDetailFragmentBinding
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
        binding.viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(CountdownDetailViewModel::class.java)

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

        return binding.root
    }
}




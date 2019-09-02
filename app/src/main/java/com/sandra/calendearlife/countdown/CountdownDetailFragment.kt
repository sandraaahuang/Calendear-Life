package com.sandra.calendearlife.countdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sandra.calendearlife.databinding.CountdownDetailFragmentBinding
import java.util.*

class CountdownDetailFragment : Fragment() {

    private lateinit var binding: CountdownDetailFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = CountdownDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.remindLayout.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    // Display Selected setDate in textbox
                    binding.remindDate.text=
                        "${monthOfYear + 1}, $dayOfMonth, $year, " }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        return binding.root
    }
}




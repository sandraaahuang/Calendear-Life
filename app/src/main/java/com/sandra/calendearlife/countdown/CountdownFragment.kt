package com.sandra.calendearlife.countdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.CountdownFragmentBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*



class CountdownFragment : Fragment() {

    private val viewModel: CountdownViewModel by lazy {
        ViewModelProviders.of(this).get(CountdownViewModel::class.java)
    }

    private lateinit var binding: CountdownFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = CountdownFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val addCountdownAdapter = AddCountdownAdapter(AddCountdownAdapter.OnClickListener {
            viewModel.displayCountdownDetails(it)
            Log.d("sandraaa","it = $it")
        })

        viewModel.navigateToCountdownProperty.observe(this, androidx.lifecycle.Observer {
            if ( null != it ) {
                // Must find the NavController from the Fragment
                this.findNavController().navigate(NavigationDirections.actionGlobalCountdownDetailFragment2(it))
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayCountdownDetailsComplete()
            }
        })

        binding.addCountdownRecyclerView.adapter = addCountdownAdapter

        addCountdownAdapter.notifyDataSetChanged()
        val cal = Calendar.getInstance()
        binding.editCountdownLayout.setOnClickListener {
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
        }

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }

        binding.saveLayout.setOnClickListener {

            val targetDate = binding.countdownDateInput.text.toString()
            val putInDate = Date(targetDate)

            val countdown = hashMapOf(
                "setDate" to FieldValue.serverTimestamp(),
                "title" to "${binding.countdownTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "targetDate" to java.sql.Timestamp(putInDate.time),
                "overdue" to false
            )

            viewModel.writeItem(countdown)
        }
        return binding.root
    }
}

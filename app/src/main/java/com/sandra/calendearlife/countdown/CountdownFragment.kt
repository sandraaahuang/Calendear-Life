package com.sandra.calendearlife.countdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
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
            findNavController().navigate(NavigationDirections.actionGlobalCountdownDetailFragment2())
        })

        binding.addCountdownRecyclerView.adapter = addCountdownAdapter

        addCountdownAdapter.notifyDataSetChanged()
        val cal = Calendar.getInstance()
        binding.editCountdownLayout.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)

            val datepickerdialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    // Display Selected setDate in textbox
                    binding.countdownDateInput.text =
                        "$stringDate"
                }, y, m, d
            )
            datepickerdialog.show()
        }

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }



        binding.saveLayout.setOnClickListener {

            val targetDate = binding.countdownDateInput.text.toString()


            val countdown = hashMapOf(
                "setDate" to FieldValue.serverTimestamp(),
                "title" to "${binding.countdownTitleInput.text}",
                "note" to targetDate.getDateWithServerTimeStamp(),
                "targetDate" to "${binding.countdownDateInput.text}",
                "overdue" to false
            )

            viewModel.writeItem(countdown)
        }



        return binding.root
    }

    /** Converting from String to Date **/
    fun String.getDateWithServerTimeStamp(): Date? {
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        )
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        try {
            return dateFormat.parse(this)
        } catch (e: ParseException) {
            return null
        }
    }
    /** Converting from Date to String**/
    fun Date.getStringTimeStampWithDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(this)
    }

}

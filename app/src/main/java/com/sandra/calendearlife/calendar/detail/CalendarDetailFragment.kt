package com.sandra.calendearlife.calendar.detail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.databinding.CalendarDetailFragmentBinding
import java.sql.Timestamp
import java.util.*

class CalendarDetailFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = CalendarDetailFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val application = requireNotNull(activity).application
        val calendar = CalendarDetailFragmentArgs.fromBundle(arguments!!).calendar
        val viewModelFactory = CalendarDetailFactory(calendar, application)
        val viewModel = ViewModelProviders.of(
            this, viewModelFactory).get(CalendarDetailViewModel::class.java)
        binding.viewModel = viewModel

        binding.allDaySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.endDate.visibility = View.GONE
                binding.beginTime.visibility = View.GONE
                binding.endTime.visibility = View.GONE
            }
            else {
                binding.endDate.visibility = View.VISIBLE
                binding.beginTime.visibility = View.VISIBLE
                binding.endTime.visibility = View.VISIBLE
            }
        }

        binding.switchSetAsReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.setRemindLayout.visibility = View.VISIBLE
            }
            else {
                binding.setRemindLayout.visibility = View.GONE
            }
        }

        binding.deleteButton.setOnClickListener {
            viewModel.deleteItem(calendar.documentID!!)
            Log.d("sandraaa", "delete = ${calendar.documentID}")

        }

        binding.saveButton.setOnClickListener {

//            val remindDate = binding.remindersDateInput.text.toString()
//            val beginDate = binding.beginDate.text.toString()
//            val endDate = binding.endDate.text.toString()
//
//            val putInDate = Date(remindDate)
//            val putInBeginDate = Date(beginDate)
//            val putInEndDate = Date(endDate)


            val updateRemind = hashMapOf(
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}"
//                "remindDate" to java.sql.Timestamp(putInDate.time)
            )

            val updateCalendar = hashMapOf(
//                "date" to Timestamp(putInBeginDate.time),
//                "beginDate" to Timestamp(putInBeginDate.time),
//                "endDate" to Timestamp(putInEndDate.time),
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "isAllDay" to "${binding.allDaySwitch.isChecked}",
                "hasReminders" to "${binding.switchSetAsReminder.isChecked}".toBoolean(),
                "hasCountdown" to "${binding.switchSetAsCountdown.isChecked}".toBoolean()
            )

            val updateCountdown = hashMapOf(
                "title" to "${binding.detailTitleInput.text}",
                "note" to "${binding.noteInput.text}"
//                "targetDate" to java.sql.Timestamp(putInEndDate.time)
            )

            Log.d("updateItem", "id = ${calendar.documentID}, " +
                    "updateCalendar = $updateCalendar," +
                    "updateCountdown = $updateCountdown," +
                    "updateRemind = $updateRemind")


//            viewModel.updateItem(calendar.documentID!!,updateCalendar,updateCountdown,updateRemind)
        }

        return binding.root
    }
}
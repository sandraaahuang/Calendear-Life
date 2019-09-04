package com.sandra.calendearlife.calendar

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.databinding.CalendarEventFragmentBinding
import com.sandra.calendearlife.dialog.RepeatDialog
import com.sandra.calendearlife.reminders.RemindersViewModel
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class CalendarEventFragment : Fragment() {

    var RESPONSE = "response"
    var EVALUATE_DIALOG = "evaluate_dialog"
    var REQUEST_EVALUATE = 0X110

    lateinit var binding: CalendarEventFragmentBinding

    private val viewModel: CalendarEventViewModel by lazy{
        ViewModelProviders.of(this).get(CalendarEventViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = CalendarEventFragmentBinding.inflate(inflater, container, false)

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

        binding.repeatChoose.setOnClickListener {
            val dialog = RepeatDialog()
            //setTargetFragment
            dialog.setTargetFragment(this, REQUEST_EVALUATE);
            dialog.show(fragmentManager!!, EVALUATE_DIALOG)
        }

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val monthOfYear = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val transferDay = when (dayOfWeek){
            2 -> "Monday"
            3 -> "Tuesday"
            4 -> "Wednesday"
            5 -> "Thursday"
            6 -> "Friday"
            7 -> "Saturday"
            else -> "Sunday"
        }

        val chooseDate = "$transferDay/${monthOfYear + 1 }/$dayOfMonth"


        binding.beginDate.text = chooseDate
        binding.endDate.text = chooseDate

        binding.beginDate.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd EEEE")
            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.beginDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )

            datePickerDialog.show()
        }

        binding.endDate.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd EEEE")
            val datePickerDialog= DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    val date = Date(year -1900, monthOfYear, dayOfMonth)
                    val stringDate = simpleDateFormat.format(date)
                    binding.endDate.text = "$stringDate" }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.remindersDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            binding.remindersDateInput.text = "${year}/${monthOfYear+1}/$dayOfMonth"
            binding.remindersTimeInput.text = "$hour:$minute AM"

            TimePickerDialog(it.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year, monthOfYear, dayOfMonth, hour, minute)
                val stringTime = SimpleDateFormat("hh:mm a").format(date)
                binding.remindersTimeInput.text =
                    "$stringTime" }, hour, minute, false
            ).show()

            val datePickerDialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    // Display Selected setDate in textbox
                    val date = Date(year -1900, monthOfYear, dayOfMonth, hour, minute)
                    val stringTime = SimpleDateFormat("yyyy/MM/dd").format(date)
                    binding.remindersDateInput.text=
                        "$stringTime" }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.remindersTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            TimePickerDialog(it.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year, monthOfYear, dayOfMonth, hour, minute)
                val stringTime = SimpleDateFormat("hh:mm a").format(date)
                binding.remindersTimeInput.text =
                    "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.beginTime.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("h:mm a")
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = simpleDateFormat.format(date)
                binding.beginTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.endTime.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("h:mm a")
            TimePickerDialog(this.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                val date = Date(year -1900, monthOfYear, dayOfMonth,hour, minute)
                val stringTime = simpleDateFormat.format(date)
                binding.endTime.text = "$stringTime" }, hour, minute, false
            ).show()
        }

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(fragmentManager!!, "bottom")
        }

        binding.saveText.setOnClickListener {

            val beginDate: String
            val endDate: String
            val dateFormat = SimpleDateFormat("yyyy/MM/dd EEEE hh:mm a")


            if (binding.allDaySwitch.isChecked){
                beginDate = "${binding.beginDate.text} 00:01 AM"
                endDate = "${binding.beginDate.text} 11:59 PM"
            } else {
                beginDate = "${binding.beginDate.text} ${binding.beginTime.text}"
                endDate = "${binding.endDate.text} ${binding.endTime.text}"
            }

            val parsedBeginDate = dateFormat.parse(beginDate)
            val parsedEndDate = dateFormat.parse(endDate)

            val item = hashMapOf(
                "setDate" to FieldValue.serverTimestamp(),
                "beginDate" to Timestamp(parsedBeginDate.time),
                "endDate" to Timestamp(parsedEndDate.time),
                "title" to "${binding.eventTitleInput.text}",
                "note" to "${binding.noteInput.text}",
                "isAllDay" to "${binding.allDaySwitch.isChecked}",
                "hasReminders" to "${binding.switchSetAsReminder.isChecked}",
                "hasCountdown" to "${binding.switchSetAsCountdown.isChecked}"
            )

            viewModel.writeItem(item)

            if (binding.switchSetAsReminder.isChecked){
                val remindFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a")
                val remindDate = "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"
                val parsedRemindDate = remindFormat.parse(remindDate)

                val reminders = hashMapOf(
                    "setDate" to FieldValue.serverTimestamp(),
                    "title" to "${binding.eventTitleInput.text}",
                    "setRemindDate" to true,
                    "remindDate" to Timestamp(parsedRemindDate.time),
                    "isChecked" to false,
                    "note" to "${binding.noteInput.text}",
                    "frequency" to RepeatDialog.value
                )

                viewModel.writeReminders(reminders)
            }

            if (binding.switchSetAsCountdown.isChecked){

                val countdown = hashMapOf(
                    "setDate" to FieldValue.serverTimestamp(),
                    "title" to "${binding.eventTitleInput.text}",
                    "note" to "${binding.noteInput.text}",
                    "targetDate" to Timestamp(parsedEndDate.time),
                    "overdue" to false

                )
                viewModel.writeCountdown(countdown)
            }
        }
        
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EVALUATE)
        {
            val evaluate = data?.getStringExtra(RepeatDialog().RESPONSE_EVALUATE2)

            binding.repeatChoose.text = evaluate
            val intent = Intent()
            intent.putExtra(RESPONSE, evaluate);
            activity?.setResult(Activity.RESULT_OK, intent)
        }
    }

}


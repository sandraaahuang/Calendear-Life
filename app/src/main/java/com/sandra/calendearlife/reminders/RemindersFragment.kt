package com.sandra.calendearlife.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.RemindersFragmentBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.dialog.RepeatDialog
import java.util.*

class RemindersFragment : Fragment() {

    private val viewModel: RemindersViewModel by lazy{
        ViewModelProviders.of(this).get(RemindersViewModel::class.java)
    }

    lateinit var binding: RemindersFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = RemindersFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val addRemindersAdapter = AddRemindersAdapter(AddRemindersAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalRemindersDetailFragment())
        })

        val itemTouchHelper= ItemTouchHelper(
            SwipeToDeleteReminders(addRemindersAdapter, this)
        )
        itemTouchHelper.attachToRecyclerView(binding.addRemindersRecyclerView)

        binding.addRemindersRecyclerView.adapter = addRemindersAdapter

        addRemindersAdapter.notifyDataSetChanged()

        binding.setReminderswitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.setRemindLayout.visibility = View.VISIBLE
            }
            else {
                binding.setRemindLayout.visibility = View.GONE
            }
        }

        binding.repeatChoose.setOnClickListener {
            RepeatDialog().show(this.fragmentManager!!, "center")

        }

        binding.remindersDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val monthOfYear = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val am = calendar.get(Calendar.AM_PM)
            val transferAm = when ( am ){
                0 -> "AM"
                else -> "PM"
            }

            binding.remindersDateInput.text = "${monthOfYear + 1}, $dayOfMonth, $year "
            binding.remindersTimeInput.text = "$hour : $minute"

            TimePickerDialog(it.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                binding.remindersTimeInput.text =
                    "$hour:$minute $transferAm" }, hour, minute, false
            ).show()

            val datePickerDialog = DatePickerDialog(
                it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                { _, year, monthOfYear, dayOfMonth ->
                    // Display Selected setDate in textbox
                    binding.remindersDateInput.text=
                        "${monthOfYear + 1}, $dayOfMonth, $year " }, year, monthOfYear, dayOfMonth
            )
            datePickerDialog.show()
        }

        binding.remindersTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(it.context, AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
            { view, hour, minute ->
                binding.remindersTimeInput.text =
                    "$hour : $minute" }, hour, minute, true
            ).show()
        }

        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }

        binding.saveText.setOnClickListener {
            val calendar = Calendar.getInstance()

            val reminders = hashMapOf(
                "setDate" to "${calendar.get(Calendar.MONTH)+1}," +
                        " ${calendar.get(Calendar.DAY_OF_MONTH)}, ${calendar.get(Calendar.YEAR)}",
                "title" to "${binding.remindersTitleInput.text}",
                "setRemindDate" to binding.setReminderswitch.isChecked,
                "remindDate" to "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}",
                "isChecked" to false,
                "note" to "${binding.remindersNoteInput.text}",
                "frequency" to RepeatDialog.value
            )

            viewModel.writeItem(reminders)

        }

        return binding.root
    }
}




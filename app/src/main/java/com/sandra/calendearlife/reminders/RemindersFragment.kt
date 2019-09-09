package com.sandra.calendearlife.reminders

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.firebase.firestore.FieldValue
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.RemindersFragmentBinding
import com.sandra.calendearlife.dialog.DiscardDialog
import com.sandra.calendearlife.dialog.RepeatDialog
import kotlinx.android.synthetic.main.dialog_repeat.*
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*



class RemindersFragment : Fragment() {

    var RESPONSE = "response"
    var EVALUATE_DIALOG = "evaluate_dialog"
    var REQUEST_EVALUATE = 0X110


    private val viewModel: RemindersViewModel by lazy{
        ViewModelProviders.of(this).get(RemindersViewModel::class.java)
    }

    lateinit var binding: RemindersFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RemindersFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel



        val addRemindersAdapter = AddRemindersAdapter(AddRemindersAdapter.OnClickListener{
            viewModel.displayReminderDetails(it)
            Log.d("sandraaa","it = $it")
        }, viewModel)

        viewModel.navigateToReminderProperty.observe(this, androidx.lifecycle.Observer {
            if ( null != it ) {
                // Must find the NavController from the Fragment
                this.findNavController().navigate(NavigationDirections.actionGlobalRemindersDetailFragment(it))
                // Tell the ViewModel we've made the navigate call to prevent multiple navigation
                viewModel.displayReminderDetailsComplete()
            }
        })

        val itemTouchHelper= ItemTouchHelper(
            SwipeToDeleteReminders(addRemindersAdapter, viewModel)
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
            val dialog = RepeatDialog()
            dialog.setTargetFragment(this, REQUEST_EVALUATE);
            dialog.show(fragmentManager!!, EVALUATE_DIALOG)
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



        binding.removeIcon.setOnClickListener {
            DiscardDialog().show(this.fragmentManager!!, "show")
        }

        binding.saveText.setOnClickListener {
            val remindDate = "${binding.remindersDateInput.text} ${binding.remindersTimeInput.text}"
            val date = "${binding.remindersDateInput.text}"
            val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a")
            val customFormat = SimpleDateFormat("yyyy/MM/dd")
            val parsedDate = dateFormat.parse(remindDate)
            val parsed = customFormat.parse(date)

            val calendar = hashMapOf(
                "color" to "C02942",
                "setDate" to FieldValue.serverTimestamp(),
                "beginDate" to java.sql.Timestamp(parsedDate.time),
                "endDate" to java.sql.Timestamp(parsedDate.time),
                "date" to java.sql.Timestamp(parsed.time),
                "title" to "${binding.remindersTitleInput.text}",
                "note" to "${binding.remindersNoteInput.text}",
                "hasReminders" to true,
                "frequency" to RepeatDialog.value
            )

            val reminders = hashMapOf(
                "setDate" to FieldValue.serverTimestamp(),
                "title" to "${binding.remindersTitleInput.text}",
                "setRemindDate" to binding.setReminderswitch.isChecked,
                "remindDate" to Timestamp(parsedDate.time),
                "isChecked" to false,
                "note" to "${binding.remindersNoteInput.text}",
                "frequency" to RepeatDialog.value
            )

            viewModel.writeItem(calendar,reminders)

        }

        return binding.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EVALUATE)
        {
            val evaluate = data?.getStringExtra(RepeatDialog().RESPONSE_EVALUATE)

            binding.repeatChoose.text = evaluate
            val intent = Intent()
            intent.putExtra(RESPONSE, evaluate);
            activity?.setResult(Activity.RESULT_OK, intent)
        }
    }

}










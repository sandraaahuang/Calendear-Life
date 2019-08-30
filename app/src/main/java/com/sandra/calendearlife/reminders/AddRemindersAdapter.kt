package com.sandra.calendearlife.reminders

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ItemAddRemindersBinding
import com.sandra.calendearlife.databinding.ItemRemindersBinding
import kotlinx.android.synthetic.main.item_reminders.view.*
import java.util.*


private const val ITEM_VIEW_TYPE_OLD = 0x01
private const val ITEM_VIEW_TYPE_ADDED = 0x00

class AddRemindersAdapter(val onClickListener: OnClickListener) : ListAdapter<Reminders, RecyclerView.ViewHolder>(DiffCallback) {

    class OnClickListener(val clickListener: (reminders: Reminders) -> Unit) {
        fun onClick(reminders: Reminders) = clickListener(reminders)
    }

//    var reminders: ArrayList<Reminders>? = null

//    override fun getItemCount(): Int {
//
//        reminders?.let {
//            return when (it.size) {
//                it.size + 1
//            }
//        }
//        Log.d("sandraaa","reminders = $reminders")
//        return 0
//    }

    companion object DiffCallback : DiffUtil.ItemCallback<Reminders>() {
        override fun areItemsTheSame(oldItem: Reminders, newItem: Reminders): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Reminders, newItem: Reminders): Boolean {
            return oldItem == newItem
        }
    }

    class ItemViewHolder(private var binding: ItemRemindersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminders: Reminders, onClickListener: OnClickListener) {
            binding.reminders = reminders
            binding.remindersTitle.setOnClickListener {
                binding.remindersInfo.visibility = View.VISIBLE
            }
            binding.root.setOnClickListener { onClickListener.onClick(reminders) }
            binding.executePendingBindings()

        }
    }

    class AddItemViewHolder(private var binding: ItemAddRemindersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
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

                TimePickerDialog(it.context,AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
                { view, hour, minute ->
                    binding.remindersTimeInput.text =
                        "$hour:$minute $transferAm" }, hour, minute, false
                ).show()

                val datePickerDialog = DatePickerDialog(
                    it.context,AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                    { _, year, monthOfYear, dayOfMonth ->
                        // Display Selected date in textbox
                        binding.remindersDateInput.text=
                            "${monthOfYear + 1}, $dayOfMonth, $year, " }, year, monthOfYear, dayOfMonth
                )
                datePickerDialog.show()
            }

            binding.remindersTimeInput.setOnClickListener {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val am = calendar.get(Calendar.AM_PM)
                val transferAm = when ( am ){
                    0 -> "AM"
                    else -> "PM"
                }
                TimePickerDialog(it.context,AlertDialog.THEME_HOLO_DARK, TimePickerDialog.OnTimeSetListener
                { view, hour, minute ->
                    binding.remindersTimeInput.text =
                        "$hour : $minute $transferAm" }, hour, minute, false
                ).show()
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ITEM_VIEW_TYPE_OLD -> ItemViewHolder(
                ItemRemindersBinding.inflate
                    (LayoutInflater.from(parent.context), parent, false)
            )
            else -> AddItemViewHolder(
                ItemAddRemindersBinding.inflate
                    (LayoutInflater.from(parent.context), parent, false)
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> holder.bind(getItem(position), onClickListener)

            is AddItemViewHolder -> holder.bind()
        }



    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            1 -> ITEM_VIEW_TYPE_ADDED
            else -> ITEM_VIEW_TYPE_OLD
        }
    }
}
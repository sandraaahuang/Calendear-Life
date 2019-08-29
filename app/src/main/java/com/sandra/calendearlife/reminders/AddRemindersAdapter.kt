package com.sandra.calendearlife.reminders

import android.app.DatePickerDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ItemAddRemindersBinding
import com.sandra.calendearlife.databinding.ItemRemindersBinding
import java.util.*

private const val ITEM_VIEW_TYPE_OLD = 0x01
private const val ITEM_VIEW_TYPE_ADDED = 0x00

class AddRemindersAdapter : ListAdapter<Reminders, RecyclerView.ViewHolder>(DiffCallback) {

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

        fun bind(reminders: Reminders?) {
            binding.reminders = reminders
            binding.executePendingBindings()

        }
    }

    class AddItemViewHolder(private var binding: ItemAddRemindersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.remindersDateInput.setOnClickListener {
                val cal = Calendar.getInstance()
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH)
                val d = cal.get(Calendar.DAY_OF_MONTH)

                val datepickerdialog = DatePickerDialog(
                    it.context, DatePickerDialog.OnDateSetListener
                    { _, year, monthOfYear, dayOfMonth ->

                        // Display Selected date in textbox
                        binding.remindersDateInput.text =
                            "${monthOfYear + 1}, $dayOfMonth, $year"
                    }, y, m, d
                )
                datepickerdialog.show()
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
            is ItemViewHolder -> holder.bind(getItem(position))
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
package com.sandra.calendearlife.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ItemHistoryRemindersBinding

class HistoryRemindersAdapter :
    ListAdapter<Reminders, HistoryRemindersAdapter.ItemViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Reminders>() {
        override fun areItemsTheSame(oldItem: Reminders, newItem: Reminders): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Reminders, newItem: Reminders): Boolean {
            return oldItem == newItem
        }
    }

    class ItemViewHolder(private var binding: ItemHistoryRemindersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminders: Reminders) {
            binding.reminders = reminders

            if (reminders.hasRemindDate){
                binding.remindersTime.visibility = View.VISIBLE
                binding.remindDateTextView.visibility = View.VISIBLE
            }
            else {
                binding.remindersTime.visibility = View.GONE
                binding.remindDateTextView.visibility = View.GONE
            }

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        return ItemViewHolder(
            ItemHistoryRemindersBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val reminders = getItem(position)
        holder.bind(reminders)
    }
}
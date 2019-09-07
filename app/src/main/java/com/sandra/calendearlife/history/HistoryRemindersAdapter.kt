package com.sandra.calendearlife.history

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.HistoryRemindersBinding
import com.sandra.calendearlife.databinding.ItemHistoryRemindersBinding
import com.sandra.calendearlife.databinding.ItemRemindersBinding
import com.sandra.calendearlife.reminders.RemindersViewModel

class HistoryRemindersAdapter() :
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
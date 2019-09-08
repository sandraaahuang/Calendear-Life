package com.sandra.calendearlife.reminders

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ItemRemindersBinding
import com.sandra.calendearlife.dialog.RepeatDialog

class AddRemindersAdapter(val onClickListener: OnClickListener, val viewModel: RemindersViewModel) :
    ListAdapter<Reminders, AddRemindersAdapter.ItemViewHolder>(DiffCallback) {

    class OnClickListener(val clickListener: (reminders: Reminders) -> Unit) {
        fun onClick(reminders: Reminders) = clickListener(reminders)
    }

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

        fun bind(reminders: Reminders, viewModel: RemindersViewModel) {
            binding.reminders = reminders
            binding.remindersChecked.setOnClickListener {
                viewModel.updateItem(reminders.documentID)
                binding.remindersTitle.setTextColor(Color.parseColor("#D8D8D8"))
                binding.checkStatus.visibility = View.VISIBLE
            }

            if (reminders.remindTimestamp.seconds < Timestamp.now().seconds){
                binding.remindersTime.setTextColor(Color.parseColor("#f44336"))
            }

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        return ItemViewHolder(
            ItemRemindersBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val reminders = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(reminders)
        }
        holder.bind(reminders, viewModel)
    }
}
package com.sandra.calendearlife.reminders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ItemRemindersBinding
import com.sandra.calendearlife.dialog.RepeatDialog

class AddRemindersAdapter(val onClickListener: OnClickListener) :
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

        fun bind(reminders: Reminders) {
            binding.reminders = reminders
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
        holder.bind(reminders)
    }
}
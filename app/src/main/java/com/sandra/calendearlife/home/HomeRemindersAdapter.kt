package com.sandra.calendearlife.home

import android.service.autofill.OnClickAction
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ItemRemindersBinding
import kotlinx.android.synthetic.main.item_reminders.view.*


class HomeRemindersAdapter(val fragment: HomeFragment, val onClickListener: OnClickListener) :
    ListAdapter<Reminders, HomeRemindersAdapter.RemindersViewHolder>(DiffCallback) {

    class RemindersViewHolder(private var binding: ItemRemindersBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reminders: Reminders) {
            binding.reminders = reminders
            binding.remindersTitle.setOnClickListener {
                binding.remindersInfo.visibility = View.VISIBLE
            }
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Reminders>() {
        override fun areItemsTheSame(oldItem: Reminders, newItem: Reminders): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Reminders, newItem: Reminders): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RemindersViewHolder {
        return RemindersViewHolder(
            ItemRemindersBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RemindersViewHolder, position: Int) {
        val reminders = getItem(position)
        holder.itemView.remindersInfo.setOnClickListener {
            onClickListener.onClick(reminders)
        }
        holder.bind(reminders)
    }

    class OnClickListener(val clickListener: (reminders: Reminders) -> Unit) {
        fun onClick(reminders: Reminders) = clickListener(reminders)
    }

}


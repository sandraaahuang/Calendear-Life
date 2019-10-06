package com.sandra.calendearlife.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.ItemRemindersBinding


class HomeRemindersAdapter(val viewModel: HomeViewModel, private val onClickListener: OnClickListener) :
    ListAdapter<Reminders, HomeRemindersAdapter.RemindersViewHolder>(DiffCallback) {

    class RemindersViewHolder(private var binding: ItemRemindersBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reminders: Reminders, viewModel: HomeViewModel) {
            binding.reminders = reminders
            binding.remindersChecked.setOnClickListener {
                viewModel.updateCheckedReminders(reminders.documentID)
                binding.remindersTitle.setTextColor(MyApplication.instance.getColor(R.color.primary_gray))
                binding.checkStatus.visibility = View.VISIBLE

            }
            if (reminders.remindTimestamp.seconds < Timestamp.now().seconds){
                binding.remindersTime.setTextColor(MyApplication.instance.getColor(R.color.delete_red))
            }

            if (!reminders.hasRemindDate){
                binding.remindersTime.visibility = View.GONE
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
        holder.itemView.setOnClickListener {
            onClickListener.onClick(reminders)
        }
        holder.bind(reminders, viewModel)
    }

    class OnClickListener(val clickListener: (reminders: Reminders) -> Unit) {
        fun onClick(reminders: Reminders) = clickListener(reminders)
    }

}


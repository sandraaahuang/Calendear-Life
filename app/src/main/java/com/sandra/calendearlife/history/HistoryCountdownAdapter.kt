package com.sandra.calendearlife.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.ItemHistoryCountdownBinding

class HistoryCountdownAdapter :
    ListAdapter<Countdown, HistoryCountdownAdapter.ItemViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<Countdown>() {
        override fun areItemsTheSame(oldItem: Countdown, newItem: Countdown): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Countdown, newItem: Countdown): Boolean {
            return oldItem == newItem
        }
    }

    class ItemViewHolder(private var binding: ItemHistoryCountdownBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(countdown: Countdown) {
            binding.countdown = countdown

            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        return ItemViewHolder(
            ItemHistoryCountdownBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val countdown = getItem(position)
        holder.bind(countdown)
    }
}
package com.sandra.calendearlife.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.ItemCountdownBinding

class HomeCountdownAdapter :
    ListAdapter<Countdown, HomeCountdownAdapter.CountdownViewHolder>(DiffCallback) {

    class CountdownViewHolder(private var binding: ItemCountdownBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(countdown: Countdown) {
            binding.countdown = countdown
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Countdown>() {
        override fun areItemsTheSame(oldItem: Countdown, newItem: Countdown): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Countdown, newItem: Countdown): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CountdownViewHolder {
        return CountdownViewHolder(
            ItemCountdownBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CountdownViewHolder, position: Int) {
        val countdown = getItem(position)
        holder.bind(countdown)
    }

}
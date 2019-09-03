package com.sandra.calendearlife.countdown

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.ItemCountdownBinding

class AddCountdownAdapter(val onClickListener: OnClickListener) :
    ListAdapter<Countdown, RecyclerView.ViewHolder>(DiffCallback) {

    class OnClickListener(val clickListener: (countdown: Countdown) -> Unit) {
        fun onClick(countdown: Countdown) = clickListener(countdown)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Countdown>() {
        override fun areItemsTheSame(oldItem: Countdown, newItem: Countdown): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Countdown, newItem: Countdown): Boolean {
            return oldItem == newItem
        }
    }

    class ItemViewHolder(private var binding: ItemCountdownBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(countdown: Countdown, onClickListener: OnClickListener) {
            binding.countdown = countdown
            binding.root.setOnClickListener { onClickListener.onClick(countdown) }
            binding.executePendingBindings()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ItemViewHolder(
            ItemCountdownBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ItemViewHolder -> holder.bind(getItem(position), onClickListener)
        }
    }
}
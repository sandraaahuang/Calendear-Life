package com.sandra.calendearlife.countdown

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.ItemCountdownBinding

class AddCountdownAdapter(val onClickListener: OnClickListener, val viewModel: CountdownViewModel) :
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

        fun bind(countdown: Countdown, onClickListener: OnClickListener, viewModel: CountdownViewModel) {
            binding.countdown = countdown
            binding.countdownDate.text =
                "倒數 " + "${((countdown.targetTimestamp.seconds - Timestamp.now().seconds) / 86400)}" + " 天"

            if (countdown.targetTimestamp.seconds < Timestamp.now().seconds) {
                viewModel.updateItem(countdown.documentID)

            }
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
            is ItemViewHolder -> holder.bind(getItem(position), onClickListener, viewModel)
        }
    }
}
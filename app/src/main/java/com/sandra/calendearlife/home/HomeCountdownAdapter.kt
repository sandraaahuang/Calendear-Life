package com.sandra.calendearlife.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.ItemCountdownBinding

class HomeCountdownAdapter(val onClickListener: OnClickListener, val viewModel: HomeViewModel) :
    ListAdapter<Countdown, HomeCountdownAdapter.CountdownViewHolder>(DiffCallback) {

    class CountdownViewHolder(private var binding: ItemCountdownBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(countdown: Countdown, viewModel: HomeViewModel) {
            binding.countdown = countdown
            binding.countdownDate.text = "${((countdown.targetTimestamp.seconds - Timestamp.now().seconds)/86400)}↓"
            Log.d("sandraaa","binding.countdownDate.text = ${binding.countdownDate.text}")

            if (countdown.targetTimestamp.seconds < Timestamp.now().seconds){
                viewModel.updateCountdown(countdown.documentID)
                Log.d("sandraaa", "countdown.targetTimestamp.seconds = ${countdown.targetTimestamp.seconds}," +
                        "Timestamp.now().seconds = ${Timestamp.now().seconds}")
            }
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
        holder.itemView.setOnClickListener {
            onClickListener.onClick(countdown)
        }
        holder.bind(countdown,viewModel)
    }

    class OnClickListener(val clickListener: (countdown: Countdown) -> Unit) {
        fun onClick(countdown: Countdown) = clickListener(countdown)
    }

}
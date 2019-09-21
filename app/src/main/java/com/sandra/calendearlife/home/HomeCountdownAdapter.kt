package com.sandra.calendearlife.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.ItemCountdownBinding
import com.sandra.calendearlife.util.getString

class HomeCountdownAdapter(val onClickListener: OnClickListener, val viewModel: HomeViewModel) :
    ListAdapter<Countdown, HomeCountdownAdapter.CountdownViewHolder>(DiffCallback) {

    class CountdownViewHolder(private var binding: ItemCountdownBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(countdown: Countdown, viewModel: HomeViewModel) {
            binding.countdown = countdown
            binding.countdownDate.text =
                "${((countdown.targetTimestamp.seconds - Timestamp.now().seconds)/86400)} ${getString(R.string.days)}"

            if (countdown.targetTimestamp.seconds < Timestamp.now().seconds){
                viewModel.updateCountdown(countdown.documentID)
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
package com.sandra.calendearlife.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.ItemCountdownBinding
import com.yy.mobile.rollingtextview.CharOrder
import com.yy.mobile.rollingtextview.strategy.Strategy


class HomeCountdownAdapter(val onClickListener: OnClickListener, val viewModel: HomeViewModel) :
    ListAdapter<Countdown, HomeCountdownAdapter.CountdownViewHolder>(DiffCallback) {

    class CountdownViewHolder(private var binding: ItemCountdownBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(countdown: Countdown, viewModel: HomeViewModel) {
            binding.countdownDate.setText("${((countdown.targetTimestamp.seconds - Timestamp.now().seconds)/86400)+4}")
            binding.countdownDate.setText("${((countdown.targetTimestamp.seconds - Timestamp.now().seconds)/86400)+3}")
            binding.countdownDate.setText("${((countdown.targetTimestamp.seconds - Timestamp.now().seconds)/86400)+2}")
            binding.countdownDate.setText("${((countdown.targetTimestamp.seconds - Timestamp.now().seconds)/86400)+1}")
            binding.countdown = countdown
            binding.countdownDate.animationDuration = 2000L
            binding.countdownDate.charStrategy = Strategy.NormalAnimation()
            binding.countdownDate.addCharOrder(CharOrder.Number)
            binding.countdownDate.animationInterpolator = AccelerateDecelerateInterpolator()
            binding.countdownDate.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                //finsih
            }
        })
            binding.countdownDate.setText("${((countdown.targetTimestamp.seconds - Timestamp.now().seconds)/86400)}")

            if (countdown.targetTimestamp.seconds < Timestamp.now().seconds){
                viewModel.updateCountdown(countdown.documentID)
            }

            if ((countdown.targetTimestamp.seconds - Timestamp.now().seconds)/86400 == 0L) {
                binding.countdownDate.setText("1")
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
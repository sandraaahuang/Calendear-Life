package com.sandra.calendearlife.calendar.month

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.CalendarShowEventBinding
import com.sandra.calendearlife.databinding.ItemCountdownBinding
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.calendar_show_event.*

class CalendarMonthAdapter(val onClickListener: OnClickListener) :
    ListAdapter<Calendar, CalendarMonthAdapter.CountdownViewHolder>(DiffCallback) {

    class CountdownViewHolder(private var binding: CalendarShowEventBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(calendar: Calendar) {
            binding.calendar = calendar

            binding.typeText.text =
                when (calendar.color){
                "af8eb5" -> "Event"
                "a094b7" -> "Reminder"
                "82b3c9" -> "Countdown"
                else -> "coming soon"
            }

            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Calendar>() {
        override fun areItemsTheSame(oldItem: Calendar, newItem: Calendar): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Calendar, newItem: Calendar): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CountdownViewHolder {
        return CountdownViewHolder(
            CalendarShowEventBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CountdownViewHolder, position: Int) {
        val calendar = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(calendar)
        }
        holder.bind(calendar)
    }

    class OnClickListener(val clickListener: (calendar: Calendar) -> Unit) {
        fun onClick(calendar: Calendar) = clickListener(calendar)
    }

}
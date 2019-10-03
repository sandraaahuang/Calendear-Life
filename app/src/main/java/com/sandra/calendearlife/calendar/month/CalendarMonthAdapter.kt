package com.sandra.calendearlife.calendar.month

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_COUNTDOWN
import com.sandra.calendearlife.constant.FirebaseKey.Companion.COLOR_REMIND
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.databinding.ItemCalendarEventBinding

class CalendarMonthAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<Calendar, CalendarMonthAdapter.CountdownViewHolder>(DiffCallback) {

    class CountdownViewHolder(private var binding: ItemCalendarEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(calendar: Calendar) {
            binding.calendar = calendar

            binding.typeText.text =
                when (calendar.color) {
                    COLOR_REMIND -> MyApplication.instance.getString(R.string.new_reminder)
                    COLOR_COUNTDOWN -> MyApplication.instance.getString(R.string.new_countdown)
                    else -> MyApplication.instance.getString(R.string.new_event)
                }

            // set countdown icon
            binding.hasCountdown.visibility =
                when {
                    calendar.hasCountdown -> View.VISIBLE
                    else -> View.GONE
                }

            // set reminder icon
            binding.hasReminder.visibility =
                when {
                    calendar.hasReminders -> View.VISIBLE
                    else -> View.GONE
                }

            binding.hasGoogle.visibility =
                when {
                    calendar.fromGoogle -> View.VISIBLE
                    else -> View.GONE
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountdownViewHolder {
        return CountdownViewHolder(
            ItemCalendarEventBinding.inflate(
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
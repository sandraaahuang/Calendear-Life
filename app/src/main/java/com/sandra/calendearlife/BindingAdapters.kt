package com.sandra.calendearlife

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sandra.calendearlife.calendar.month.CalendarMonthAdapter
import com.sandra.calendearlife.countdown.AddCountdownAdapter
import com.sandra.calendearlife.data.Calendar
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.history.HistoryCountdownAdapter
import com.sandra.calendearlife.history.HistoryCountdownViewModel
import com.sandra.calendearlife.history.HistoryRemindersAdapter
import com.sandra.calendearlife.home.HomeCountdownAdapter
import com.sandra.calendearlife.home.HomeRemindersAdapter
import com.sandra.calendearlife.reminders.AddRemindersAdapter

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .into(imgView)
    }
}

@BindingAdapter("listCountdown")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Countdown>?) {
    recyclerView.adapter?.apply {
        when (this) {
            is HomeCountdownAdapter -> {
                submitList(data)
            }
            is AddCountdownAdapter -> {
                submitList(data)
            }
            is HistoryCountdownAdapter -> {
                submitList(data)
            }
        }
    }
}

@BindingAdapter("listReminder")
fun bindremindersRecyclerView(recyclerView: RecyclerView, data: List<Reminders>?) {
    recyclerView.adapter?.apply {
        when (this) {
            is HomeRemindersAdapter -> {
                submitList(data)
            }
            is AddRemindersAdapter -> {
                submitList(data)
            }
            is HistoryRemindersAdapter -> {
                submitList(data)
            }
        }
    }
}


@BindingAdapter("listEvent")
fun bindEventRecyclerView(recyclerView: RecyclerView, data: List<Calendar>?) {
    recyclerView.adapter?.apply {
        when (this) {
            is CalendarMonthAdapter -> {
                submitList(data)
            }
        }
    }
}
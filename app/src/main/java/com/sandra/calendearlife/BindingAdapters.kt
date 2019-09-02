package com.sandra.calendearlife

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.home.HomeCountdownAdapter
import com.sandra.calendearlife.home.HomeRemindersAdapter

@BindingAdapter("listCountdown")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Countdown>?) {
    val adapter = recyclerView.adapter as HomeCountdownAdapter
    data?.let {
        adapter.submitList(it)
    }
}

@BindingAdapter("listReminder")
fun bindremindersRecyclerView(recyclerView: RecyclerView, data: List<Reminders>?) {
    val adapter = recyclerView.adapter as HomeRemindersAdapter
    data?.let {
        adapter.submitList(it)
    }
}
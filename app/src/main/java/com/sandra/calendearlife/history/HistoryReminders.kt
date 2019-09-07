package com.sandra.calendearlife.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.sandra.calendearlife.databinding.HistoryRemindersBinding

class HistoryReminders : Fragment() {

    private val viewModel: HistoryRemindersViewModel by lazy{
        ViewModelProviders.of(this).get(HistoryRemindersViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = HistoryRemindersBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.historyReminderRecyclerview.adapter = HistoryRemindersAdapter()

        return binding.root
    }
}
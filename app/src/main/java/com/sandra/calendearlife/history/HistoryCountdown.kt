package com.sandra.calendearlife.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.sandra.calendearlife.databinding.HistoryCountdownBinding

class HistoryCountdown : Fragment() {

    private val viewModel: HistoryCountdownViewModel by lazy{
        ViewModelProviders.of(this).get(HistoryCountdownViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = HistoryCountdownBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.historyReminderRecyclerview.adapter = HistoryCountdownAdapter()

        return binding.root
    }
}
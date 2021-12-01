package com.sandra.calendearlife.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.FragmentHistoryRemindersBinding

class HistoryRemindersFragment : Fragment() {

    private val viewModel: HistoryRemindersViewModel by lazy{
        ViewModelProviders.of(this).get(HistoryRemindersViewModel::class.java)
    }
    private var adapter: HistoryRemindersAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentHistoryRemindersBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        adapter = HistoryRemindersAdapter()
        binding.historyReminderRecyclerview.adapter = adapter

        viewModel.liveReminders.observe(viewLifecycleOwner, Observer {
            adapter?.submitList(it)
            adapter?.notifyDataSetChanged()
        })

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        return binding.root
    }
}
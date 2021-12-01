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
import com.sandra.calendearlife.databinding.FragmentHistoryCountdownBinding

class HistoryCountdownFragment : Fragment() {

    private val viewModel: HistoryCountdownViewModel by lazy{
        ViewModelProviders.of(this).get(HistoryCountdownViewModel::class.java)
    }

    private var adapter: HistoryCountdownAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentHistoryCountdownBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        adapter = HistoryCountdownAdapter()
        binding.historyCountdownRecyclerview.adapter = adapter

        viewModel.liveCountdown.observe(viewLifecycleOwner, Observer {
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
package com.sandra.calendearlife.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSnapHelper
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.constant.Const.Companion.TYPE_HOME
import com.sandra.calendearlife.databinding.FragmentHomeBinding
import com.sandra.calendearlife.constant.Const.Companion.putType


class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by lazy{
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val countdownAdapter = HomeCountdownAdapter(HomeCountdownAdapter.OnClickListener{
            putType(TYPE_HOME)
            findNavController().navigate(NavigationDirections.actionGlobalCountdownDetailFragment2(it))
        },viewModel)

        val remindersAdapter = HomeRemindersAdapter(viewModel, HomeRemindersAdapter.OnClickListener{
            putType(TYPE_HOME)
            findNavController().navigate(NavigationDirections.actionGlobalRemindersDetailFragment(it))
        })
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.countdownRecyclerView.adapter = countdownAdapter
        binding.remindersRecyclerView.adapter = remindersAdapter

        ItemTouchHelper(SwipeToDeleteCallback(remindersAdapter, viewModel)).apply {
            attachToRecyclerView(binding.remindersRecyclerView)
        }

        LinearSnapHelper().apply {
            attachToRecyclerView(binding.countdownRecyclerView)
        }
        
        binding.indicator.attachToRecyclerView(binding.countdownRecyclerView)

        // floating action button
        val fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        val rotateForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        val rotateBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)
        var isOpen = false

        binding.fabAdd.setOnClickListener {

            isOpen = if (isOpen) {

                binding.fabAdd.startAnimation(rotateBackward)
                binding.remindersFab.startAnimation(fabClose)
                binding.countdownsFab.startAnimation(fabClose)
                binding.calendarFab.startAnimation(fabClose)
                binding.addReminderText.startAnimation(fabClose)
                binding.addCountdownText.startAnimation(fabClose)
                binding.addEventText.startAnimation(fabClose)
                false

            } else {
                binding.fabAdd.startAnimation(rotateForward)
                binding.remindersFab.startAnimation(fabOpen)
                binding.countdownsFab.startAnimation(fabOpen)
                binding.calendarFab.startAnimation(fabOpen)
                binding.addReminderText.startAnimation(fabOpen)
                binding.addCountdownText.startAnimation(fabOpen)
                binding.addEventText.startAnimation(fabOpen)
                true
            }
        }
        binding.remindersFab.setOnClickListener {
            putType(TYPE_HOME)
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        binding.countdownsFab.setOnClickListener {
            putType(TYPE_HOME)
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.calendarFab.setOnClickListener {
            putType(TYPE_HOME)
            findNavController().navigate(NavigationDirections.actionGlobalCalendarEventFragment())
        }

        binding.noCountdown.setOnClickListener {
            putType(TYPE_HOME)
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.noReminder.setOnClickListener {
            putType(TYPE_HOME)
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }

        binding.swipeFreshHome.setOnRefreshListener {
            viewModel.getItem()
        }

        viewModel.isRefreshing.observe(this , Observer {
            it?.let {
                binding.swipeFreshHome.isRefreshing = false
            }
        })

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        return binding.root
    }
}



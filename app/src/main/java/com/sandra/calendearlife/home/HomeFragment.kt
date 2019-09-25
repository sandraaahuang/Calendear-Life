package com.sandra.calendearlife.home


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSnapHelper
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.HomeFragmentBinding
import com.sandra.calendearlife.util.FragmentType


class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by lazy{
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    lateinit var binding: HomeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val countdownAdapter = HomeCountdownAdapter(HomeCountdownAdapter.OnClickListener{
            putType("home")
            findNavController().navigate(NavigationDirections.actionGlobalCountdownDetailFragment2(it))
        },viewModel)

        val remindersAdapter = HomeRemindersAdapter(viewModel, HomeRemindersAdapter.OnClickListener{
            putType("home")
            findNavController().navigate(NavigationDirections.actionGlobalRemindersDetailFragment(it))
        })
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val itemTouchHelper= ItemTouchHelper(
            SwipeToDeleteCallback(
                remindersAdapter,
                viewModel
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.remindersRecyclerView)

        binding.countdownRecyclerView.adapter = countdownAdapter
        binding.remindersRecyclerView.adapter = remindersAdapter

        LinearSnapHelper().apply {
            attachToRecyclerView(binding.countdownRecyclerView)
        }

        val recyclerIndicator = binding.indicator
        recyclerIndicator.attachToRecyclerView(binding.countdownRecyclerView)

        // floating action button
        binding.remindersFab.setOnClickListener {
            putType("home")
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        binding.countdownsFab.setOnClickListener {
            putType("home")
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.calendarFab.setOnClickListener {
            putType("calendar")
            findNavController().navigate(NavigationDirections.actionGlobalCalendarEventFragment())
        }

        binding.noCountdown.setOnClickListener {
            putType("home")
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.noReminder.setOnClickListener {
            putType("home")
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }

        return binding.root
    }

    private fun putType (type: String) {
        val preferences =
            MyApplication.instance.
                getSharedPreferences("fragment", Context.MODE_PRIVATE)
        preferences.edit().putString("type", type).apply()
        preferences.getString("type","")
        FragmentType.type = type
        Log.d("sandraaa", "type = ${FragmentType.type}")
    }
}



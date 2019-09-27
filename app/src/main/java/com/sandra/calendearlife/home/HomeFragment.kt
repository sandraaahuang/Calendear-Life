package com.sandra.calendearlife.home


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearSnapHelper
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.HomeFragmentBinding
import com.sandra.calendearlife.util.FragmentType
import com.yy.mobile.rollingtextview.CharOrder
import com.yy.mobile.rollingtextview.RollingTextView
import com.yy.mobile.rollingtextview.strategy.Strategy
import kotlinx.android.synthetic.main.item_countdown.view.*


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
        val fabOpen = AnimationUtils.loadAnimation(this.context, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(this.context, R.anim.fab_close)
        val rotateForward = AnimationUtils.loadAnimation(this.context, R.anim.rotate_forward)
        val rotateBackward = AnimationUtils.loadAnimation(this.context, R.anim.rotate_backward)
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
            putType("home")
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        binding.countdownsFab.setOnClickListener {
            putType("home")
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.calendarFab.setOnClickListener {
            putType("home")
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



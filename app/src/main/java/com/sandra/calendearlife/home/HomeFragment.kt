package com.sandra.calendearlife.home


import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.provider.FontsContractCompat.FontRequestCallback.RESULT_OK
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.AccountPicker
import com.google.firebase.firestore.FirebaseFirestore
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.HomeFragmentBinding
import com.google.android.gms.auth.api.signin.GoogleSignInAccount



class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by lazy{
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    lateinit var binding: HomeFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val countdownAdapter = HomeCountdownAdapter(HomeCountdownAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalCountdownDetailFragment2(it))
        },viewModel)

        val remindersAdapter = HomeRemindersAdapter(viewModel, HomeRemindersAdapter.OnClickListener{
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

        val recyclerIndicator = binding.indicator
        recyclerIndicator.attachToRecyclerView(binding.countdownRecyclerView)

        // floating action button
        binding.remindersFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        binding.countdownsFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        binding.calendarFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalCalendarEventFragment())
        }

        return binding.root
    }
}



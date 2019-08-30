package com.sandra.calendearlife.home


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.HomeFragmentBinding
import com.sandra.calendearlife.databinding.NavHeaderMainBinding
import kotlinx.android.synthetic.main.item_reminders.view.*

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by lazy{
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    lateinit var binding: HomeFragmentBinding
    lateinit var mockdata2: ArrayList<Reminders>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val countdownAdapter = HomeCountdownAdapter()
        val remindersAdapter = HomeRemindersAdapter(this, HomeRemindersAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalRemindersDetailFragment())
        })
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        viewModel

        val itemTouchHelper= ItemTouchHelper(
            SwipeToDeleteCallback(
                remindersAdapter,
                this
            )
        )
        itemTouchHelper.attachToRecyclerView(binding.remindersRecyclerView)

        //mock data
        val mockData = ArrayList<Countdown>()
        mockData.add(Countdown("88", "倒數數起來^^", "20200101"))
        mockData.add(Countdown("99", "倒數數起來:D", "20210101"))

        mockdata2 = ArrayList()
        mockdata2.add(Reminders("please remind me!!!", "20201010", false, false))
        mockdata2.add(Reminders("who am I!!!", "20201012", false, false))

        binding.countdownRecyclerView.adapter = countdownAdapter
        binding.remindersRecyclerView.adapter = remindersAdapter

        countdownAdapter.submitList(mockData)
        remindersAdapter.submitList(mockdata2)


        Log.d("sandraaa","mockData = $mockData")

        // floating action button
        binding.remindersFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalRemindersFragment())
        }
        binding.countdownsFab.setOnClickListener {
            findNavController().navigate(NavigationDirections.actionGlobalCountdownFragment())
        }

        return binding.root
    }
}



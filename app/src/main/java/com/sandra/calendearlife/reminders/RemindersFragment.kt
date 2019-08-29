package com.sandra.calendearlife.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.RemindersFragmentBinding
import com.sandra.calendearlife.home.SwipeToDeleteCallback

class RemindersFragment : Fragment() {

    private lateinit var binding: RemindersFragmentBinding
    val mockdata2 = ArrayList<Reminders>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = RemindersFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val addRemindersAdapter = AddRemindersAdapter()

        val itemTouchHelper= ItemTouchHelper(
            SwipeToDeleteReminders(addRemindersAdapter, this)
        )
        itemTouchHelper.attachToRecyclerView(binding.addRemindersRecyclerView)


        mockdata2.add(Reminders("please remind me!!!", "20201010", false, false))
        mockdata2.add(Reminders("who am I!!!", "20201012", false, false))

        binding.addRemindersRecyclerView.adapter = addRemindersAdapter
        addRemindersAdapter.submitList(mockdata2)
        addRemindersAdapter.notifyDataSetChanged()




        return binding.root
    }
}




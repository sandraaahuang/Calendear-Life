package com.sandra.calendearlife.reminders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.RemindersFragmentBinding
import com.sandra.calendearlife.home.HomeViewModel
import com.sandra.calendearlife.home.SwipeToDeleteCallback
import kotlinx.android.synthetic.main.calendar_event_fragment.view.*
import kotlinx.android.synthetic.main.item_add_reminders.view.*

class RemindersFragment : Fragment() {

    private val viewModel: RemindersViewModel by lazy{
        ViewModelProviders.of(this).get(RemindersViewModel::class.java)
    }

    lateinit var binding: RemindersFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = RemindersFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val addRemindersAdapter = AddRemindersAdapter(AddRemindersAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalRemindersDetailFragment())
        }, this, viewModel)

        val itemTouchHelper= ItemTouchHelper(
            SwipeToDeleteReminders(addRemindersAdapter, this)
        )
        itemTouchHelper.attachToRecyclerView(binding.addRemindersRecyclerView)

        binding.addRemindersRecyclerView.adapter = addRemindersAdapter

        addRemindersAdapter.notifyDataSetChanged()

        return binding.root
    }
}




package com.sandra.calendearlife.countdown

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.data.Reminders
import com.sandra.calendearlife.databinding.CountdownFragmentBinding
import com.sandra.calendearlife.databinding.RemindersFragmentBinding
import com.sandra.calendearlife.reminders.AddRemindersAdapter
import kotlinx.android.synthetic.main.item_add_countdown.view.*
import java.util.*
import javax.xml.datatype.DatatypeConstants.MONTHS

class CountdownFragment : Fragment() {

    private val viewModel: CountdownViewModel by lazy{
        ViewModelProviders.of(this).get(CountdownViewModel::class.java)
    }

    private lateinit var binding: CountdownFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = CountdownFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        viewModel

        val addCountdownAdapter = AddCountdownAdapter(AddCountdownAdapter.OnClickListener{
            findNavController().navigate(NavigationDirections.actionGlobalCountdownDetailFragment2())
        })

        //mock data
        val mockData = ArrayList<Countdown>()
        mockData.add(Countdown("88", "倒數數起來^^", "20200101"))
        mockData.add(Countdown("99", "倒數數起來:D", "20210101"))

        binding.addCountdownRecyclerView.adapter = addCountdownAdapter
        addCountdownAdapter.submitList(mockData)
        addCountdownAdapter.notifyDataSetChanged()

        return binding.root
    }
}

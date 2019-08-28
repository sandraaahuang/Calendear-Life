package com.sandra.calendearlife.home


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProviders
import com.sandra.calendearlife.R
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by lazy{
        ViewModelProviders.of(this).get(HomeViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val adapter = HomeCountdownAdapter()
        val binding = HomeFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        viewModel

        //mock data
        val mockData = ArrayList<Countdown>()
        mockData.add(Countdown("88", "倒數數起來^^", "20200101"))
        mockData.add(Countdown("99", "倒數數起來:D", "20210101"))

        binding.countdownRecyclerView.adapter = adapter

        adapter.submitList(mockData)


        Log.d("sandraaa","mockData = $mockData")

        return binding.root
    }
}



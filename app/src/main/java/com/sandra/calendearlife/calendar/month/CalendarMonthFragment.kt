package com.sandra.calendearlife.calendar.month

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.CalendarMonthFragmentBinding

class CalendarMonthFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = CalendarMonthFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }
}
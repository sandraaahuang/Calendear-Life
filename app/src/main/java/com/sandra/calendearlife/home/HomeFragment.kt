package com.sandra.calendearlife.home


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.sandra.calendearlife.R
import com.sandra.calendearlife.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = HomeFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }
}



package com.sandra.calendearlife.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sandra.calendearlife.NavigationDirections
import com.sandra.calendearlife.databinding.DialogDiscardBinding
import com.sandra.calendearlife.util.FragmentType

class DiscardDialog : BottomSheetDialogFragment() {

    lateinit var binding: DialogDiscardBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DialogDiscardBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.removeIcon2.setOnClickListener {
            this.dismiss()
        }
        binding.keepEditingLayout.setOnClickListener {
            this.dismiss()
        }

        binding.discardLayout.setOnClickListener {

            when (FragmentType.type) {
                "calendar" -> {
                    findNavController().navigate(NavigationDirections.actionGlobalCalendarMonthFragment())
                    this.dismiss()
                }
                else -> {
                    findNavController().navigate(NavigationDirections.actionGlobalHomeFragment())
                    this.dismiss()
                }
            }

        }

        return binding.root
    }
}
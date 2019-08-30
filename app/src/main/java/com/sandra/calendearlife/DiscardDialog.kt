package com.sandra.calendearlife

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sandra.calendearlife.databinding.DialogDiscardBinding

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

        return binding.root
    }
}
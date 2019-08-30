package com.sandra.calendearlife.countdown

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.data.Countdown
import com.sandra.calendearlife.databinding.ItemAddCountdownBinding
import com.sandra.calendearlife.databinding.ItemCountdownBinding
import java.util.*

private const val ITEM_VIEW_TYPE_OLD = 0x01
private const val ITEM_VIEW_TYPE_ADDED = 0x00

class AddCountdownAdapter(val onClickListener: OnClickListener) : ListAdapter<Countdown, RecyclerView.ViewHolder>(DiffCallback) {

    class OnClickListener(val clickListener: (countdown: Countdown) -> Unit) {
        fun onClick(countdown: Countdown) = clickListener(countdown)
    }
//    var reminders: ArrayList<Countdowns>? = null

//    override fun getItemCount(): Int {
//
//        reminders?.let {
//            return when (it.size) {
//                it.size + 1
//            }
//        }
//        Log.d("sandraaa","Countdown = $Countdown")
//        return 0
//    }

    companion object DiffCallback : DiffUtil.ItemCallback<Countdown>() {
        override fun areItemsTheSame(oldItem: Countdown, newItem: Countdown): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Countdown, newItem: Countdown): Boolean {
            return oldItem == newItem
        }
    }

    class ItemViewHolder(private var binding: ItemCountdownBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(countdown: Countdown, onClickListener: OnClickListener) {
            binding.countdown = countdown
            binding.root.setOnClickListener { onClickListener.onClick(countdown) }
            binding.executePendingBindings()

        }
    }

    class AddItemViewHolder(private var binding: ItemAddCountdownBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.editCountdownLayout.setOnClickListener {
                val cal = Calendar.getInstance()
                val y = cal.get(Calendar.YEAR)
                val m = cal.get(Calendar.MONTH)
                val d = cal.get(Calendar.DAY_OF_MONTH)

                val datepickerdialog = DatePickerDialog(
                    it.context, AlertDialog.THEME_HOLO_DARK, DatePickerDialog.OnDateSetListener
                    { _, year, monthOfYear, dayOfMonth ->

                        // Display Selected date in textbox
                        binding.countdownDateInput.text =
                            "${monthOfYear+1}, $dayOfMonth, $year"
                    }, y, m, d
                )

                datepickerdialog.show()
            }
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ITEM_VIEW_TYPE_OLD -> ItemViewHolder(
                ItemCountdownBinding.inflate
                    (LayoutInflater.from(parent.context), parent, false)
            )
            else -> AddItemViewHolder(
                ItemAddCountdownBinding.inflate
                    (LayoutInflater.from(parent.context), parent, false)
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ItemViewHolder -> holder.bind(getItem(position), onClickListener)
            is AddItemViewHolder -> holder.bind()
        }


    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            1 -> ITEM_VIEW_TYPE_ADDED
            else -> ITEM_VIEW_TYPE_OLD
        }
    }
}
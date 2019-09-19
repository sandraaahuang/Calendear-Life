package com.sandra.calendearlife.home


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import com.sandra.calendearlife.home.HomeFragment
import com.sandra.calendearlife.home.HomeRemindersAdapter
import com.sandra.calendearlife.reminders.RemindersViewModel
import kotlinx.android.synthetic.main.item_reminders.view.*


class SwipeToDeleteCallback(val adapter: HomeRemindersAdapter, val viewModel: HomeViewModel) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private lateinit var icon: Drawable

    private val background: ColorDrawable = ColorDrawable(MyApplication.instance.getColor(R.color.delete_red))


    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // used for up and down movements
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        viewModel.remindersItem.removeAt(position)
        val title = viewHolder.itemView.remindersTitle.text.toString()
        viewModel.deleteItem(title)
        adapter.notifyDataSetChanged()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        icon = MyApplication.instance.getDrawable(R.drawable.icon_delete)!!
        val itemView = viewHolder.itemView
        val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight
        val backgroundCornerOffset = 20

        if (dX > 0) { // Swiping to the right
            icon = MyApplication.instance.getDrawable(R.drawable.icon_delete)!!
            val iconLeft = itemView.left + iconMargin + icon.intrinsicWidth
            val iconRight = itemView.left + iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            background.setBounds(
                itemView.left, itemView.top,
                itemView.left + dX.toInt() + backgroundCornerOffset,
                itemView.bottom
            )
        } else if (dX < 0) { // Swiping to the left
            val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            background.setBounds(
                itemView.right + dX.toInt() - backgroundCornerOffset,
                itemView.top, itemView.right, itemView.bottom
            )
        } else { // view is unSwiped
            background.setBounds(0, 0, 0, 0)
        }

        background.draw(c)
        icon.draw(c)
    }


}



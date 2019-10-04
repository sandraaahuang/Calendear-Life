package com.sandra.calendearlife.home


import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.MyApplication
import com.sandra.calendearlife.R
import kotlinx.android.synthetic.main.item_reminders.view.*


class SwipeToDeleteCallback(private val adapter: HomeRemindersAdapter, val viewModel: HomeViewModel) :
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

        viewModel.remindersItem.removeAt(viewHolder.adapterPosition)
        viewModel.swipe2deleteRemindersItem(viewHolder.itemView.remindersTitle.text.toString())
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
        MyApplication.instance.getDrawable(R.drawable.icon_delete)?.let {
            icon = it
        }
        val iconMargin = (viewHolder.itemView.height - icon.intrinsicHeight) / 2
        val iconTop = viewHolder.itemView.top + (viewHolder.itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight
        val backgroundCornerOffset = 20

        when {
            dX > 0 -> { // Swiping to the right

                MyApplication.instance.getDrawable(R.drawable.icon_delete)?.let {
                    icon = it
                }

                icon.setBounds(
                    viewHolder.itemView.left + iconMargin + icon.intrinsicWidth,
                    iconTop,
                    viewHolder.itemView.left + iconMargin,
                    iconBottom
                )

                background.setBounds(
                    viewHolder.itemView.left,
                    viewHolder.itemView.top,
                    viewHolder.itemView.left + dX.toInt() + backgroundCornerOffset,
                    viewHolder.itemView.bottom
                )
            }
            dX < 0 -> { // Swiping to the left

                icon.setBounds(
                    viewHolder.itemView.right - iconMargin - icon.intrinsicWidth,
                    iconTop,
                    viewHolder.itemView.right - iconMargin,
                    iconBottom
                )

                background.setBounds(
                    viewHolder.itemView.right + dX.toInt() - backgroundCornerOffset,
                    viewHolder.itemView.top,
                    viewHolder.itemView.right,
                    viewHolder.itemView.bottom
                )
            }
            else -> // view is unSwiped
                background.setBounds(0, 0, 0, 0)
        }

        background.draw(c)
        icon.draw(c)
    }


}



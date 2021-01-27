package com.example.android.interviewdiary.fragments.recordList

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.android.interviewdiary.R

class RecordListItemTouchHelper (private val recordListItemTouchHelperAdapter: RecordListItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {

        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

        return if (viewHolder.itemViewType == ITEM_VIEW_TYPE_RECORD_LIST_RECORD)
            makeMovementFlags(0, swipeFlags)
        else
            makeMovementFlags(0, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        recordListItemTouchHelperAdapter.onItemSwipe(viewHolder.adapterPosition)
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
        val itemView = viewHolder.itemView

        var swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#FF0000"))
        var editIcon: Drawable =
            ContextCompat.getDrawable(viewHolder.itemView.context, R.drawable.ic_delete)!!
        val iconMarginVertical =
            (viewHolder.itemView.height - editIcon.intrinsicHeight) / 2

        if (dX > 0) {
            swipeBackground.setBounds(
                itemView.left,
                itemView.top,
                dX.toInt(),
                itemView.bottom
            )
            editIcon.setBounds(
                itemView.left + iconMarginVertical,
                itemView.top + iconMarginVertical,
                itemView.left + iconMarginVertical + editIcon.intrinsicWidth,
                itemView.bottom - iconMarginVertical
            )
        } else {
            swipeBackground.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            editIcon.setBounds(
                itemView.right - iconMarginVertical - editIcon.intrinsicWidth,
                itemView.top + iconMarginVertical,
                itemView.right - iconMarginVertical,
                itemView.bottom - iconMarginVertical
            )
            editIcon.level = 0
        }
        c.save()

        swipeBackground.draw(c)

        if (dX > 0)
            c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
        else
            c.clipRect(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )

        editIcon.draw(c)

        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }
}


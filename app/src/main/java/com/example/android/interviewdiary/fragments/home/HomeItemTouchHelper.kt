package com.example.android.interviewdiary.fragments.home

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.android.interviewdiary.R

class HomeItemTouchHelper(private val homeItemTouchHelperAdapter: HomeItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {

        val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

        return if (viewHolder.itemViewType == ITEM_VIEW_TYPE_ADD)
            makeMovementFlags(0, 0)
        else
            makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        dragged: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when (direction) {
            ItemTouchHelper.LEFT -> homeItemTouchHelperAdapter.onItemSwipeLeft(viewHolder.adapterPosition)
            ItemTouchHelper.RIGHT -> homeItemTouchHelperAdapter.onItemSwipeRight(viewHolder.adapterPosition)
        }
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

//        val paint = Paint()
//        paint.color = Color.BLACK
//        paint.textSize = 100f
//        paint.textAlign = Paint.Align.CENTER
//
//        c.drawText("hallo", itemView.left +  , itemView.top + itemView.height / 2f, paint)

        var swipeBackground: Drawable
        val editIcon = ContextCompat.getDrawable(
            viewHolder.itemView.context,
            R.drawable.ic_edit
        )!!
        val interviewIcon = ContextCompat.getDrawable(
            viewHolder.itemView.context,
            R.drawable.ic_question_answer
        )!!

        val interviewIconMarginVertical =
            (viewHolder.itemView.height - interviewIcon.intrinsicHeight) / 2

        val editIconMarginVertical =
            (viewHolder.itemView.height - interviewIcon.intrinsicHeight) / 2

        if (dX > 0) {
            swipeBackground = ContextCompat.getDrawable(itemView.context, R.drawable.background_swipe_right__home)!!
            swipeBackground.setBounds(
                itemView.left,
                itemView.top,
                dX.toInt(),
                itemView.bottom
            )
            interviewIcon.setBounds(
                itemView.left + interviewIconMarginVertical,
                itemView.top + interviewIconMarginVertical,
                itemView.left + interviewIconMarginVertical + interviewIcon.intrinsicWidth,
                itemView.bottom - interviewIconMarginVertical
            )
            interviewIcon.setTint(ContextCompat.getColor(itemView.context, R.color.primary_color_dark))
        } else {
            swipeBackground = ContextCompat.getDrawable(itemView.context, R.drawable.background_swipe_left__home)!!
            swipeBackground.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            editIcon.setBounds(
                itemView.right - editIconMarginVertical - editIcon.intrinsicWidth,
                itemView.top + editIconMarginVertical,
                itemView.right - editIconMarginVertical,
                itemView.bottom - editIconMarginVertical
            )
            editIcon.setTint(ContextCompat.getColor(itemView.context, R.color.black))
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
        interviewIcon.draw(c)


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


package com.example.android.interviewdiary.fragments.addEdit

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.android.interviewdiary.R

class AddEditItemTouchHelper(private val addEditItemTouchHelperAdapter: AddEditItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {


    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return if (viewHolder.itemViewType == VIEW_TYPE_ADD_EDIT_ANSWER_OPTION)
            makeMovementFlags(dragFlags, 0)
        else
            makeMovementFlags(0, 0)
    }

    override fun canDropOver(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return (current.itemViewType == target.itemViewType)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        dragged: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        addEditItemTouchHelperAdapter.onItemMove(dragged.adapterPosition, target.adapterPosition)
        return true
    }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG)
            viewHolder!!.itemView.background = ContextCompat.getDrawable(viewHolder.itemView.context, R.color.light_gray)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        addEditItemTouchHelperAdapter.onItemMoveFinish()
        viewHolder.itemView.background = ContextCompat.getDrawable(viewHolder.itemView.context, R.color.white)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        return
    }

}


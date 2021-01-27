package com.example.android.interviewdiary.fragments.addEdit

interface AddEditItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int)
    fun onItemMoveFinish()
}
package com.example.android.interviewdiary.fragments.home

interface HomeItemTouchHelperAdapter {
//    fun onItemMove(fromPosition: Int, toPosition: Int)
//    fun onItemSwipe(position: Int)
    fun onItemSwipeLeft(adapterPosition: Int)
    fun onItemSwipeRight(adapterPosition: Int)
}
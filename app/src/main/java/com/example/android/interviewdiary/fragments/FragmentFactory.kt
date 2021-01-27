package com.example.android.interviewdiary.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.fragments.addEdit.AddEditAdapter
import com.example.android.interviewdiary.fragments.addEdit.AddEditFragment
import com.example.android.interviewdiary.fragments.home.HomeDateAdapter
import com.example.android.interviewdiary.fragments.home.HomeFragment
import com.example.android.interviewdiary.fragments.home.HomeListAdapter
import com.example.android.interviewdiary.fragments.interview.InterviewMultipleChoiceFragment
import com.example.android.interviewdiary.fragments.interview.InterviewNumericFragment
import com.example.android.interviewdiary.fragments.interview.InterviewTimeFragment
import com.example.android.interviewdiary.fragments.other.BackupFragment
import com.example.android.interviewdiary.fragments.recordList.RecordListAdapter
import com.example.android.interviewdiary.fragments.recordList.RecordListFragment
import javax.inject.Inject

class FragmentFactory @Inject constructor(
    private val glide: RequestManager,
    private val homeListAdapter: HomeListAdapter,
    private val homeDateAdapter: HomeDateAdapter,
    private val recordListAdapter: RecordListAdapter,
    private val addEditAdapter: AddEditAdapter
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            HomeFragment::class.java.name -> HomeFragment(homeListAdapter, homeDateAdapter)

            RecordListFragment::class.java.name -> RecordListFragment(
                recordListAdapter,
                glide
            )

            AddEditFragment::class.java.name -> AddEditFragment(addEditAdapter)

            InterviewNumericFragment::class.java.name -> InterviewNumericFragment(glide)

            InterviewTimeFragment::class.java.name -> InterviewTimeFragment(glide)

            InterviewMultipleChoiceFragment::class.java.name -> InterviewMultipleChoiceFragment(
                glide
            )

            BackupFragment::class.java.name -> BackupFragment()

            else -> super.instantiate(classLoader, className)
        }
    }
}


package com.example.android.interviewdiary.fragments.interview

import androidx.lifecycle.SavedStateHandle
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.model.TrackerType
import com.example.android.interviewdiary.other.Constants
import com.example.android.interviewdiary.repositories.FakeAppRepository
import org.junit.Assert.*
import org.junit.Before
import java.time.LocalDate

// TODO Dive into the topic "Testing" and add test cases


class InterviewNavigationViewModelTest {

    private lateinit var viewModel: InterviewNavigationViewModel

    @Before
    fun setup() {

        val fakeSavedStateHandle = SavedStateHandle()

        fakeSavedStateHandle.set(Constants.DATE, LocalDate.now())
        fakeSavedStateHandle.set(
            Constants.TRACKER_IDS,
            intArrayOf(1, 2, 3)
        )

//        viewModel = InterviewNavigationViewModel(FakeAppRepository(),fakeSavedStateHandle)
    }
}
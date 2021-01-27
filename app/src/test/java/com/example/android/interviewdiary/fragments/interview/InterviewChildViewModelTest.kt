package com.example.android.interviewdiary.fragments.interview

import androidx.lifecycle.SavedStateHandle
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.model.TrackerType
import com.example.android.interviewdiary.other.Constants
import com.example.android.interviewdiary.repositories.FakeAppRepository
import org.junit.Before
import java.time.LocalDate

// TODO Dive into the topic "Testing" and add test cases


class InterviewChildViewModelTest {

    private lateinit var viewModel: InterviewChildViewModel

    @Before
    fun setup() {
        val fakeSavedStateHandle = SavedStateHandle()

        fakeSavedStateHandle.set(Constants.DATE, LocalDate.now())
        fakeSavedStateHandle.set(
            Constants.TRACKER, Tracker(
                trackerId = 1,
                question = "This is a fake question",
                type = TrackerType.MULTIPLE_CHOICE,
                imageUri = "",
                configValues = listOf(0, 1, 2, 3, 4),
                answerOptions = mapOf(1 to "one", 2 to "two", 3 to "three"),
                multiSelectionEnabled = false,
            )
        )

        viewModel = InterviewChildViewModel(FakeAppRepository(),fakeSavedStateHandle)
    }
}
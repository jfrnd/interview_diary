package com.example.android.interviewdiary.fragments.addEdit

import androidx.lifecycle.SavedStateHandle
import com.example.android.interviewdiary.model.TrackerType
import com.example.android.interviewdiary.repositories.FakeAppRepository
import com.example.android.interviewdiary.other.Constants
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat


class AddEditViewModelTest {

// TODO Dive into the topic "Testing" and add test cases

    private lateinit var viewModel: AddEditViewModel

    @Before
    fun setup() {

        val fakeSavedStateHandle = SavedStateHandle()
        fakeSavedStateHandle.set(Constants.TRACKER_TYPE, TrackerType.MULTIPLE_CHOICE)
        fakeSavedStateHandle.set(Constants.QUESTION, "This is a fake question")

        viewModel = AddEditViewModel(FakeAppRepository(), fakeSavedStateHandle)
    }

    @Test
    fun `add button creates new answer option`() {
        viewModel.onAddButtonClick()

        println("add button creates new answer option")
        println(viewModel.answerOptions.value)

        assertThat(viewModel.answerOptions.value == mapOf(1 to "")).isTrue()

    }
}
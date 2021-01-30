package com.example.android.interviewdiary.fragments.addEdit

import android.util.Log
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
    fun `onAddClick adds a new answer option`() {
        val sizeInTheBeginning = viewModel.answerOptions.value.size
        viewModel.onAddButtonClick()
        val sizeAfterOnAddClick = viewModel.answerOptions.value.size
        assertThat(sizeInTheBeginning + 1 == sizeAfterOnAddClick).isTrue()
    }
}
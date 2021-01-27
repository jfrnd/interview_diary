package com.example.android.interviewdiary.fragments.home

import com.example.android.interviewdiary.repositories.FakeAppRepository
import org.junit.Before

// TODO Dive into "Testing" and add test cases

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel(FakeAppRepository())
    }
}
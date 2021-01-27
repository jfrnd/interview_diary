package com.example.android.interviewdiary.fragments

import android.content.Context
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * If using Hilt, this custom NavHostFragment is necessary, in order to avoid a crash during configuration change.
 * Reason: Hilt is doing the injection before onCreate() is called.
 */
@AndroidEntryPoint
class MainNavHostFragment: NavHostFragment() {

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.fragmentFactory = fragmentFactory
    }
}
package com.example.android.interviewdiary.fragments.other

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentAboutBinding

class AboutFragment : Fragment(R.layout.fragment_about) {

    private lateinit var binding: FragmentAboutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding =  FragmentAboutBinding.bind(view)

    }
}

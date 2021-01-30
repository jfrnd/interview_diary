package com.example.android.interviewdiary.fragments.interview

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentInterviewNumericBinding
import com.example.android.interviewdiary.databinding.FragmentInterviewYesNoBinding
import com.example.android.interviewdiary.model.Feature
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class InterviewYesNoFragment @Inject constructor(
    private val glide: RequestManager
) : Fragment(R.layout.fragment_interview_yes_no) {

    private lateinit var binding: FragmentInterviewYesNoBinding

    private val childViewModel: InterviewChildViewModel by viewModels()
    private val navigationViewModel: InterviewNavigationViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentInterviewYesNoBinding.bind(view)

        setBindingObject()

        setObserver()

        (parentFragment as InterviewNavigationFragment).apply {
            setControlBar(childViewModel)
            setChildFragmentEventListener(childViewModel)
        }
    }

    private fun setBindingObject() {

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            childViewModel.streamCurrentNote().collect { note ->
                when {
                    !childViewModel.tracker.enabledFeatures.contains(Feature.NOTES) -> binding.header.boxNote.visibility =
                        View.GONE

                    note.isBlank() -> binding.header.apply {
                        icEdit.visibility = View.INVISIBLE
                        icAdd.visibility = View.VISIBLE
                        boxNote.setOnClickListener {
                            navigationViewModel.onNoteClick()
                        }
                    }
                    else -> binding.header.apply {
                        icEdit.visibility = View.VISIBLE
                        icAdd.visibility = View.INVISIBLE
                        tvNote.text = note
                        boxNote.setOnClickListener {
                            navigationViewModel.onNoteClick()
                        }
                    }
                }
            }
        }

        glide.load(childViewModel.tracker.imageUri).into(binding.header.ivInterview)

        binding.header.tvQuestion.text = childViewModel.tracker.question

        binding.body.apply {
            btnYes.setOnClickListener {
                childViewModel.onYesClick(navigationViewModel.forceNoteInputInCurrentSession)
            }
            btnNo.setOnClickListener {
                childViewModel.onNoClick(navigationViewModel.forceNoteInputInCurrentSession)

            }
        }
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            childViewModel.streamCurrentValues().collect { curValues ->
                binding.body.btnNo.isChecked = curValues.contains(0)
                binding.body.ivSaveNo.isVisible = curValues.contains(0)
                binding.body.btnYes.isChecked = curValues.contains(1)
                binding.body.ivSaveYes.isVisible = curValues.contains(1)
            }
        }
    }
}
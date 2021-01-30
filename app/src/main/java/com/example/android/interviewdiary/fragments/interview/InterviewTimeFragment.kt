package com.example.android.interviewdiary.fragments.interview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentInterviewTimeBinding
import com.example.android.interviewdiary.model.Feature
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class InterviewTimeFragment @Inject constructor(
    private val glide: RequestManager,
) : Fragment(R.layout.fragment_interview_time) {

    private lateinit var binding: FragmentInterviewTimeBinding

    private val childViewModel: InterviewChildViewModel by viewModels()
    private val navigationViewModel: InterviewNavigationViewModel by viewModels(ownerProducer = { requireParentFragment() })


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentInterviewTimeBinding.bind(view)

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
            npHh.apply {
                setOnValueChangedListener { picker, _, _ ->
                    childViewModel.onNumericTimeValueChanged(0, picker.value.toFloat())
                }
                setFormatter { value ->
                    value.toString().padStart(2, '0')
                }
            }
            npMm.apply {
                setOnValueChangedListener { picker, _, _ ->
                    childViewModel.onNumericTimeValueChanged(1, picker.value.toFloat())
                }
                setFormatter { value ->
                    value.toString().padStart(2, '0')
                }
            }
            npSs.apply {
                setOnValueChangedListener { picker, _, _ ->
                    childViewModel.onNumericTimeValueChanged(2, picker.value.toFloat())
                }
                setFormatter { value ->
                    value.toString().padStart(2, '0')
                }
            }
        }
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            childViewModel.streamCurrentValues().collect { curValues ->
                binding.body.apply {
                    if (curValues.isNotEmpty()) {
                        curValues[0].let {
                            npHh.value = it.toInt()
                        }
                        curValues[1].let {
                            npMm.value = it.toInt()
                        }
                        curValues[2].let {
                            npSs.value = it.toInt()
                        }
                    }
                }
            }
        }
    }
}
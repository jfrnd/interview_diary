package com.example.android.interviewdiary.fragments.interview

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentInterviewNumericBinding
import com.example.android.interviewdiary.model.Feature
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class InterviewNumericFragment @Inject constructor(
    private val glide: RequestManager
) : Fragment(R.layout.fragment_interview_numeric) {

    private lateinit var binding: FragmentInterviewNumericBinding

    private val childViewModel: InterviewChildViewModel by viewModels()
    private val navigationViewModel: InterviewNavigationViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentInterviewNumericBinding.bind(view)

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
            tvMin.text = childViewModel.tracker.configValues[1].toString()
            tvMax.text = childViewModel.tracker.configValues[2].toString()
            tvUnit.text = childViewModel.tracker.unit
            slider.apply {
                value = childViewModel.tracker.configValues[1].toFloat()
                valueTo = childViewModel.tracker.configValues[2].toFloat()
                valueFrom = childViewModel.tracker.configValues[1].toFloat()
                addOnChangeListener { _, value, _ ->
                    childViewModel.onNumericTimeValueChanged(0, value.toInt())
                }
            }
            btnPlus.setOnClickListener {
                if (slider.value < slider.valueTo) slider.value++
            }
            btnMinus.setOnClickListener {
                if (slider.valueFrom < slider.value) slider.value--
            }
        }
    }

    private fun setObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            childViewModel.streamCurrentValues().collect { curValues ->
                binding.body.slider.apply {
                    if (curValues.isNotEmpty()) {
                        if (curValues[0] >= valueFrom.toInt() && curValues[0] <= valueTo.toInt()) {
                            value = curValues[0].toFloat()
                            binding.body.tvVal.text = curValues[0].toString()
                        } else {
                            value = childViewModel.tracker.configValues[0].toFloat()
                            binding.body.tvVal.text =
                                childViewModel.tracker.configValues[0].toString()
                        }
                    }
                }
            }
        }
    }
}
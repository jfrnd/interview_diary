package com.example.android.interviewdiary.fragments.interview

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentInterviewMultipleChoiceBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class InterviewMultipleChoiceFragment @Inject constructor(
    private val glide: RequestManager,
) : Fragment(R.layout.fragment_interview_multiple_choice) {

    lateinit var binding: FragmentInterviewMultipleChoiceBinding

    private val childViewModel: InterviewChildViewModel by viewModels()
    private val navigationViewModel: InterviewNavigationViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentInterviewMultipleChoiceBinding.bind(view)

        val interviewMultipleChoiceAdapter = InterviewMultipleChoiceAdapter(glide)

        setAdapter(interviewMultipleChoiceAdapter)

        setBindingObject(interviewMultipleChoiceAdapter)

        (parentFragment as InterviewNavigationFragment).apply {
            setControlBar(childViewModel)
            setChildFragmentEventListener(childViewModel)
        }
    }

    private fun setAdapter(interviewMultipleChoiceAdapter: InterviewMultipleChoiceAdapter) {
        interviewMultipleChoiceAdapter.setOnItemClickListener { answerId ->
            childViewModel.onAnswerClick(answerId)
        }

        interviewMultipleChoiceAdapter.setOnNoteClickListener { ->
            navigationViewModel.onNoteClick()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            childViewModel.createAdapterItemList().collect() {
                interviewMultipleChoiceAdapter.createAndSubmitList(it)
            }
        }
    }

    private fun setBindingObject(interviewMultipleChoiceAdapter: InterviewMultipleChoiceAdapter) {
        binding.apply {
            rvInterviewMultipleChoice.apply {
                this.adapter = interviewMultipleChoiceAdapter
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }
}



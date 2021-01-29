package com.example.android.interviewdiary.fragments.interview

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.android.interviewdiary.NavGraphDirections
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentInterviewNavigationBinding
import com.example.android.interviewdiary.other.Constants.NOTE_DIALOG_MODE
import com.example.android.interviewdiary.other.utils.ConverterUtil.toDisplayedString
import com.example.android.interviewdiary.other.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

// TODO Make it possible to navigate back from AddEdit Fragment

@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class InterviewNavigationFragment : Fragment(R.layout.fragment_interview_navigation) {

    private val navigationViewModel: InterviewNavigationViewModel by viewModels(ownerProducer = { this })

    private lateinit var binding: FragmentInterviewNavigationBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentInterviewNavigationBinding.bind(view)

        (activity as AppCompatActivity).supportActionBar?.title =
            navigationViewModel.date.toDisplayedString(true, requireContext())

        setNavigationEventListener()

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu_interview, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_menu_edit_tracker -> {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(getString(R.string.dialog_title_edit_tracker))
                builder.setMessage(getString(R.string.dialog_confirm_edit_tracker))
                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    navigationViewModel.onEditTrackerClick()
                }
                builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
                builder.create().show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setNavigationEventListener() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            navigationViewModel.event.collect { event ->
                when (event) {
                    is InterviewNavigationViewModel.NavigationEvent.LoadNewQuestion -> loadNewQuestion(
                        event.newFragment,
                        event.animation
                    )
                    is InterviewNavigationViewModel.NavigationEvent.NavigateHome -> findNavController().popBackStack()
                    is InterviewNavigationViewModel.NavigationEvent.NavigateToEditTracker -> {
                        val action =
                            NavGraphDirections.actionGlobalAddUpdateFragment(
                                event.tracker,
                                title = getString(R.string.fragment_title_edit_tracker)
                            )
                        findNavController().popBackStack()
                        findNavController().navigate(action)
                    }
                    is InterviewNavigationViewModel.NavigationEvent.OpenNoteDialog -> {
                        val dialog = InterviewAddEditNoteDialog().apply {
                            arguments = Bundle().apply {
                                putBoolean(NOTE_DIALOG_MODE, event.forced)
                            }
                        }
                        dialog.show(childFragmentManager, "SetNoteDialog")
                    }
                }.exhaustive
            }
        }
    }

    fun setChildFragmentEventListener(childViewModel: InterviewChildViewModel) {

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            childViewModel.recordEvent.collect { event ->
                when (event) {
                    InterviewChildViewModel.ChildEvent.ForceOpenNoteDialog -> navigationViewModel.forceOpenNoteDialog()
                    InterviewChildViewModel.ChildEvent.NavigateForward -> navigationViewModel.navigateForward()
                    InterviewChildViewModel.ChildEvent.NavigateBack -> navigationViewModel.navigateBack()
                }.exhaustive
            }
        }
    }

    private fun loadNewQuestion(
        fragment: Fragment,
        animation: InterviewNavigationViewModel.Animation?
    ) {
        childFragmentManager.beginTransaction().apply {
            when (animation) {
                InterviewNavigationViewModel.Animation.NEXT -> this.setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                InterviewNavigationViewModel.Animation.BACK -> this.setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                else -> null
            }.exhaustive
            replace(R.id.fm_interview, fragment)
            commitNow()
        }
    }

    fun setControlBar(childViewModel: InterviewChildViewModel) {

        binding.controlBarInterview.fabConfirm.setOnClickListener {
            childViewModel.onConfirmClick(forceNoteInputInCurrentSession = navigationViewModel.forceNoteInputInCurrentSession)
        }
        binding.controlBarInterview.fabBack.setOnClickListener {
            childViewModel.onBackClick()
        }
        binding.controlBarInterview.fabCancel.setOnClickListener {
            childViewModel.onCancelClick()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            childViewModel.streamSelectionStatus().collect { status ->
                when (status) {
                    InputStatus.WAITING_FOR_INPUT -> binding.controlBarInterview.apply {
                        fabConfirm.visibility = View.GONE
                        fabBack.visibility =
                            if (!navigationViewModel.isSingleQuestion() && !navigationViewModel.isFirstQuestion())
                                View.VISIBLE else View.GONE
                        fabCancel.visibility = View.GONE
                    }
                    InputStatus.INPUT_OK_AND_OR_SYNC_TO_DATABASE_VALUES -> binding.controlBarInterview.apply {
                        fabConfirm.visibility = View.VISIBLE
                        fabBack.visibility =
                            if (!navigationViewModel.isSingleQuestion() && !navigationViewModel.isFirstQuestion())
                                View.VISIBLE else View.GONE
                        fabCancel.visibility = View.GONE
                    }
                    InputStatus.INPUT_ASYNC_TO_DATABASE_VALUES -> binding.controlBarInterview.apply {
                        fabConfirm.visibility = View.VISIBLE
                        fabBack.visibility = View.GONE
                        fabCancel.visibility = View.VISIBLE
                    }
                }
            }.exhaustive
        }
    }
}




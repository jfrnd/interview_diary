package com.example.android.interviewdiary.fragments.interview

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.DialogInterviewNoteBinding
import com.example.android.interviewdiary.other.Constants.NOTE_DIALOG_MODE
import com.example.android.interviewdiary.other.utils.ViewUtils.focusAndShowKeyboard
import kotlinx.coroutines.flow.collect

class InterviewAddEditNoteDialog : DialogFragment(R.layout.dialog_interview_note) {

    private val navigationViewModel: InterviewNavigationViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val childViewModel: InterviewChildViewModel by viewModels(ownerProducer = { requireParentFragment().childFragmentManager.fragments[0] })

    lateinit var binding: DialogInterviewNoteBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DialogInterviewNoteBinding.bind(view)

        val forcedInput = requireArguments().getBoolean(NOTE_DIALOG_MODE)

        binding.apply {
            etNote.addTextChangedListener {
                if (binding.etNote.text.toString().isNotEmpty())
                    binding.apply {
                        btnSkip.isVisible = false
                        btnConfirmAndNavigateFurther.isVisible = forcedInput
                    } else binding.apply {
                    btnSkip.isVisible = forcedInput
                    btnConfirmAndNavigateFurther.isVisible = false
                }

                cbNeverAskAgain.isVisible = forcedInput && !navigationViewModel.isSingleQuestion()
                btnConfirm.isVisible = !forcedInput
                btnCancel.isVisible = !forcedInput
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                childViewModel.streamCurrentNote().collect { note ->
                    binding.etNote.apply {
                        setText(note)
                        focusAndShowKeyboard()
                        selectAll()
                        if (note.isBlank())
                            binding.etNote.hint = getString(R.string.interview_note_example)
                        else
                            binding.etNote.hint = note
                    }
                }
            }

            binding.apply {
                btnConfirmAndNavigateFurther.setOnClickListener {
                    val input = binding.etNote.text.toString().trim()
                    childViewModel.onEditNoteConfirmAndSaveClick(input)
                    navigationViewModel.onEditNoteDialogConfirmClick(binding.cbNeverAskAgain.isChecked)
                    dismiss()
                }

                btnSkip.setOnClickListener {
                    val input = binding.etNote.text.toString().trim()
                    childViewModel.onSkipNoteClick()
                    navigationViewModel.onEditNoteDialogConfirmClick(binding.cbNeverAskAgain.isChecked)
                    dismiss()
                }

                btnConfirm.setOnClickListener {
                    val input = binding.etNote.text.toString().trim()
                    childViewModel.onEditNoteConfirmClick(input)
                    dismiss()
                }
                btnCancel.setOnClickListener {
                    dismiss()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
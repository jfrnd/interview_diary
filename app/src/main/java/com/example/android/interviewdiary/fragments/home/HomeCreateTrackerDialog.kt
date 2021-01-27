package com.example.android.interviewdiary.fragments.home

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.model.TrackerType
import com.example.android.interviewdiary.databinding.DialogHomeCreateTrackerBinding
import com.example.android.interviewdiary.other.utils.ViewUtils.focusAndShowKeyboard

class HomeCreateTrackerDialog : DialogFragment(R.layout.dialog_home_create_tracker) {

    private val homeViewModel: HomeViewModel by viewModels(ownerProducer = { requireParentFragment() })

    lateinit var binding: DialogHomeCreateTrackerBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DialogHomeCreateTrackerBinding.bind(view)

        binding.apply {

            btnConfirm.setOnClickListener {
                val questionInput = binding.etValue.text.toString().trim()
                val trackerType: TrackerType? = when {
                    binding.rbMultipleChoice.isChecked -> TrackerType.MULTIPLE_CHOICE
                    binding.rbNumeric.isChecked -> TrackerType.NUMERIC
                    binding.rbTime.isChecked -> TrackerType.TIME
                    else -> null
                }
                if (homeViewModel.onCreateTrackerConfirmClick(questionInput, trackerType))
                    dismiss()
            }

            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        binding.radioGroup.clearCheck()
    }

    override fun onStart() {
        binding.apply {
            etValue.focusAndShowKeyboard()
            etValue.selectAll()
        }
        super.onStart()
        val width = (resources.displayMetrics.widthPixels)
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
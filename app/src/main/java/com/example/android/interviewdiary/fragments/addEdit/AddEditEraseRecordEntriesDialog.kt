package com.example.android.interviewdiary.fragments.addEdit

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.DialogAddEditEraseRecordEntriesBinding
import com.example.android.interviewdiary.other.Constants.DELETED_ANSWER_IDS

class AddEditEraseRecordEntriesDialog :
    DialogFragment(R.layout.dialog_add_edit_erase_record_entries) {

    private val viewModel: AddEditViewModel by viewModels(ownerProducer = { requireParentFragment() })

    lateinit var binding: DialogAddEditEraseRecordEntriesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DialogAddEditEraseRecordEntriesBinding.bind(view)

        val deletedAnswerIds = arguments?.getIntArray(DELETED_ANSWER_IDS)

        val resources = requireParentFragment().requireContext().resources

        val questionPartOne =
            resources.getString(R.string.add_edit_dialog_erase_question_first) + "\n"
        var questionPartTwo = ""
        val questionPartThree =
            "\n" + resources.getString(R.string.add_edit_dialog_erase_question_second)

        deletedAnswerIds!!.forEach { answerId ->
            questionPartTwo +=
                "${viewModel.tracker!!.answerOptions[answerId]}".plus(
                    " - ".plus(
                        "${viewModel.allRecordValues!!.count { it == answerId }} ${resources.getString(R.string.add_edit_dialog_erase_question_record_counter)}\n"
                    )
                )
        }

        binding.apply {
            tvQuestion.text = questionPartOne + questionPartTwo + questionPartThree
            btnErase.setOnClickListener {
                viewModel.onEraseRecordEntriesClick(deletedAnswerIds)
                dismiss()
            }
            btnRetrieve.setOnClickListener {
                viewModel.onRetrieveAnswerOptionsClick(deletedAnswerIds)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels)
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
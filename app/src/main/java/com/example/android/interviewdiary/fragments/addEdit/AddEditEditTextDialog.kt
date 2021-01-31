package com.example.android.interviewdiary.fragments.addEdit

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.DialogAddEditEditTextBinding
import com.example.android.interviewdiary.other.Constants.ANSWER_ID
import com.example.android.interviewdiary.other.Constants.ANSWER_OPTION
import com.example.android.interviewdiary.other.Constants.DEFAULT_VALUE
import com.example.android.interviewdiary.other.Constants.INDEX
import com.example.android.interviewdiary.other.Constants.ITEM_VIEW_TYPE
import com.example.android.interviewdiary.other.Constants.MAX_VALUE
import com.example.android.interviewdiary.other.Constants.MIN_VALUE
import com.example.android.interviewdiary.other.Constants.QUESTION
import com.example.android.interviewdiary.other.Constants.UNIT
import com.example.android.interviewdiary.other.utils.ViewUtils.focusAndShowKeyboard

class AddEditEditTextDialog : DialogFragment(R.layout.dialog_add_edit_edit_text) {

    private val viewModel: AddEditViewModel by viewModels(ownerProducer = { requireParentFragment() })

    lateinit var binding: DialogAddEditEditTextBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DialogAddEditEditTextBinding.bind(view)

        //TODO Remove decimal in et of numeric

        val itemViewType = requireArguments().getInt(ITEM_VIEW_TYPE)
        val index = requireArguments().getInt(INDEX)
        val answerId = requireArguments().getInt(ANSWER_ID)

        val data = viewModel.initEditTextDialog(itemViewType, index, answerId)

        val labelHeadline = when (data.first) {
            QUESTION -> getString(R.string.add_edit_dialog_edit_question_headline)
            ANSWER_OPTION -> getString(R.string.add_edit_dialog_edit_answer_headline)
            DEFAULT_VALUE -> getString(R.string.add_edit_dialog_edit_def_val_headline)
            MIN_VALUE -> getString(R.string.add_edit_dialog_edit_min_val_headline)
            MAX_VALUE -> getString(R.string.add_edit_dialog_edit_max_val_headline)
            UNIT -> getString(R.string.add_edit_dialog_edit_unit_headline)
            else -> "ERROR"
        }

        if (itemViewType == VIEW_TYPE_ADD_EDIT_NUMERIC)
            binding.etValue.inputType = InputType.TYPE_CLASS_NUMBER

        binding.apply {
            tvLabelHeadline.text = labelHeadline
            etValue.apply {
                hint = data.second
                setText(data.second)
                focusAndShowKeyboard()
                selectAll()
            }

            btnConfirm.setOnClickListener {
                if (viewModel.onEditTextConfirmClick(
                        binding.etValue.text.toString().trim(),
                        itemViewType,
                        index,
                        answerId
                    )
                ) dismiss()
            }

            btnCancel.setOnClickListener {
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



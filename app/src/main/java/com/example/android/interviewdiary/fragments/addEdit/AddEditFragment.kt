package com.example.android.interviewdiary.fragments.addEdit

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.interviewdiary.ADD_UPDATE_REQUEST
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentAddEditBinding
import com.example.android.interviewdiary.other.Constants.ANSWER_ID
import com.example.android.interviewdiary.other.Constants.DELETED_ANSWER_IDS
import com.example.android.interviewdiary.other.Constants.INDEX
import com.example.android.interviewdiary.other.Constants.ITEM_VIEW_TYPE
import com.example.android.interviewdiary.other.utils.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class AddEditFragment @Inject constructor(
    private val addEditAdapter: AddEditAdapter
) : Fragment(R.layout.fragment_add_edit) {

    private val viewModel: AddEditViewModel by viewModels(ownerProducer = { this })

    private lateinit var pickImages: ActivityResultLauncher<String>

    private lateinit var binding: FragmentAddEditBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pickImages =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    viewModel.changeImageUri(it.toString())
                }
            }

        binding = FragmentAddEditBinding.bind(view)

        setHasOptionsMenu(true)
        setBindingObject()
        setAdapter()
        setEventListener()
    }

    override fun onDestroyView() {
        addEditAdapter.submitList(null)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.title == getString(R.string.fragment_title_edit_tracker))
            inflater.inflate(R.menu.overflow_menu_add_edit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_menu_delete_tracker -> {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(getString(R.string.dialog_title_delete_tracker))
                builder.setMessage(getString(R.string.dialog_confirm_delete_tracker))
                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    viewModel.onDeleteClick(viewModel.tracker!!)
                }
                builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
                builder.create().show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setBindingObject() {
        binding.apply {
            fabSave.setOnClickListener {
                viewModel.onSaveClick()
            }
            rvAddEdit.apply {
                adapter = addEditAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }

    private fun setAdapter() {

        val callback = AddEditItemTouchHelper(addEditAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)

        itemTouchHelper.attachToRecyclerView(binding.rvAddEdit)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.createAdapterItemList().collect { items ->
                addEditAdapter.createAndSubmitList(items)
            }
        }

        addEditAdapter.apply {
            setItemTouchHelper(itemTouchHelper)

            //TODO override onBackClick, implement "Are you sure you want to discard changes"

            setOnItemClickListener { itemViewType, index, answerId ->
                viewModel.onItemClick(itemViewType, index, answerId)
            }

            setOnSwitchCheckedListener { adapterPosition, isChecked ->
                viewModel.updateSwitchValue(adapterPosition, isChecked)
            }

            setOnTimePickerValueChangeListener { idx, value ->
                viewModel.onTimePickerValueChange(idx, value)
            }


            setOnAddButtonClickListener {
                viewModel.onAddButtonClick()
            }

            setOnAnswerDeleteClick { answerId ->
                viewModel.onAnswerRemoveClick(answerId.toFloat())
            }

            setOnItemMoveStartListener {
                viewModel.onItemMoveStart()
            }

            setOnItemMoveListener { fromPosition, toPosition ->
                viewModel.onItemMove(fromPosition, toPosition)
            }

            setOnItemMoveFinishListener {
                viewModel.onItemMoveFinish()
            }
        }
    }

    private fun setEventListener() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.event.collect { event ->
                when (event) {
                    is AddEditViewModel.Event.OpenPickImageDialog ->
                        pickImages.launch("image/*")

                    is AddEditViewModel.Event.OpenEditTextDialog -> {
                        val dialog = AddEditEditTextDialog().apply {
                            arguments = Bundle().apply {
                                putInt(ITEM_VIEW_TYPE, event.itemViewType)
                                putInt(INDEX, event.index)
                                putInt(ANSWER_ID, event.answerId)
                            }
                        }
                        dialog.show(
                            childFragmentManager,
                            "AddEditEditTextDialog"
                        )
                    }

                    is AddEditViewModel.Event.OpenEraseRecordEntriesDialog -> {
                        val dialog = AddEditEraseRecordEntriesDialog().apply {
                            arguments = Bundle().apply {
                                putIntArray(DELETED_ANSWER_IDS, event.answerIds.map { it.toInt() }.toIntArray())
                            }
                        }
                        dialog.show(
                            childFragmentManager,
                            "EraseRecordEntriesDialog"
                        )
                    }

                    is AddEditViewModel.Event.ShowInvalidInputMessage -> {
                        Snackbar.make(
                            requireView(),
                            when (event.snackBar) {
                                InvalidInputSnackBar.STILL_LOADING_DATA -> getString(R.string.add_edit_save_invalid_input_still_loading)
                                InvalidInputSnackBar.FIELD_CANNOT_BE_EMPTY-> getString(R.string.add_edit_dialog_invalid_input_field_cannot_be_empty)
                                InvalidInputSnackBar.INPUT_MUST_BE_SHORTER -> getString(R.string.add_edit_dialog_invalid_input_shorter_input)
                                InvalidInputSnackBar.USED_INVALID_CHARACTER -> getString(R.string.add_edit_dialog_invalid_input_invalid_character)
                                InvalidInputSnackBar.CREATE_MORE_ANSWER_OPTIONS -> getString(R.string.add_edit_save_invalid_input_create_more_answer_options)
                                InvalidInputSnackBar.CHOOSE_UNIQUE_ANSWER_OPTIONS -> getString(
                                    R.string.add_edit_save_invalid_input_unique_answer_options
                                )
                                InvalidInputSnackBar.MAX_VAL_SMALLER_THAN_DEF_VAL -> getString(
                                    R.string.add_edit_save_invalid_input_max_val_smaller_than_def_val
                                )
                                InvalidInputSnackBar.MAX_VAL_SMALLER_THAN_MIN_VAL -> getString(
                                    R.string.add_edit_save_invalid_input_max_val_smaller_than_min_val
                                )
                                InvalidInputSnackBar.DEF_VAL_SMALLER_THAN_MIN_VAL -> getString(
                                    R.string.add_edit_save_invalid_input_def_val_smaller_than_min_val
                                )
                                InvalidInputSnackBar.EMPTY_FIELDS -> getString(R.string.add_edit_save_invalid_input_empty_fields)
                            }.exhaustive, Snackbar.LENGTH_LONG
                        ).show()
                    }

                    is AddEditViewModel.Event.NavigateBackWithResult -> {
                        setFragmentResult(
                            ADD_UPDATE_REQUEST,
                            bundleOf(ADD_UPDATE_REQUEST to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive
            }
        }
    }
}
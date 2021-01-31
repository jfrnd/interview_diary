package com.example.android.interviewdiary.fragments.recordList

import android.app.AlertDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.NavGraphDirections
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.other.utils.exhaustive
import com.example.android.interviewdiary.databinding.FragmentRecordListBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

// TODO Add functionality, that you can switch between the different trackers via next prev buttons on the top
// TODO Add sorting functionality
// TODO Add very simple data evaluation features (e.g. "average value", "highest value" etc.)

@AndroidEntryPoint
class RecordListFragment @Inject constructor(
    private val recordListAdapter: RecordListAdapter,
    private val glide: RequestManager
) : Fragment(R.layout.fragment_record_list) {

    private val viewModel: RecordListViewModel by viewModels()
    private lateinit var binding: FragmentRecordListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentRecordListBinding.bind(view)

        setHasOptionsMenu(true)
        setBindingObject()
        setAdapter()
        setEventListener()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu_record_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_menu_export_csv -> {
                viewModel.exportToCSVClicked(requireContext())
                true
            }

            R.id.btn_menu_edit -> {
                viewModel.onEditTrackerClick()
                true
            }

            R.id.btn_menu_clear -> {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(getString(R.string.dialog_title_clear_records))
                builder.setMessage(getString(R.string.dialog_confirm_clear_records))
                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    viewModel.onClearRecordsClick()
                }
                builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
                builder.create().show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        recordListAdapter.submitList(null)
        super.onDestroyView()
    }

    private fun setBindingObject() {
        binding.rvRecordList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0)
                    binding.header.viewGroup.offsetTopAndBottom(-dy)
                if (dy > 0)
                    binding.header.viewGroup.offsetTopAndBottom(-dy)
                if (binding.header.viewGroup.y > 0)
                    binding.header.viewGroup.y = 0F
                if (binding.header.viewGroup.y < -binding.header.viewGroup.height)
                    binding.header.viewGroup.y = -binding.header.viewGroup.height.toFloat()
            }
        })

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.streamTracker().collect { tracker ->
                binding.header.apply {
                    tvQuestion.text = tracker?.question ?: ""
                    glide.load(tracker?.imageUri).into(binding.header.ivRecordList)
                }
            }

        }

        binding.apply {
            header.apply {

                root.setOnClickListener {
                    viewModel.onEditTrackerClick()
                }
            }
            rvRecordList.apply {
                adapter = recordListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

    }

    private fun setAdapter() {
        val callback = RecordListItemTouchHelper(recordListAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)

        itemTouchHelper.attachToRecyclerView(binding.rvRecordList)

        recordListAdapter.apply {


            setOnItemClickListener { date ->
                viewModel.onItemClick(date)
            }
            setOnSwipeListener { recordId ->
                viewModel.onItemSwipe(recordId)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.streamTracker().collect { tracker ->
                recordListAdapter.setTracker(tracker)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.streamRecordList().collect { (itemList, focusedPosition) ->
                recordListAdapter.createAndSubmitList(itemList)
                binding.rvRecordList.scrollToPosition(focusedPosition - 3)
            }
        }
    }

    private fun setEventListener() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.event.collect { event ->
                when (event) {
                    is RecordListViewModel.Event.ShowGenericSnackBar -> Snackbar.make(
                        requireView(),
                        when (event.snackBar) {
                            MySnackBars.NO_RECORDS_CREATED_YET -> getString(R.string.snack_bar_all_records_cleared)
                            MySnackBars.RECORDS_CLEARED -> getString(R.string.snack_bar_all_records_cleared)
                        }.exhaustive,
                        Snackbar.LENGTH_LONG
                    ).show()

                    is RecordListViewModel.Event.ShowRecordDeletedSnackBar -> Snackbar.make(
                        requireView(),
                        getString(R.string.record_list_snack_bar_record_deleted),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(getString(R.string.snack_bar_undo)) { viewModel.onUndoClick(event.record) }
                        .show()

                    is RecordListViewModel.Event.NavigateToEditRecord -> {
                        val action =
                            RecordListFragmentDirections.actionGlobalInterviewNestedNavGraph(
                                intArrayOf(viewModel.trackerId!!), event.date.toString()
                            )
                        findNavController().navigate(action)
                    }

                    is RecordListViewModel.Event.NavigateToEditTracker -> {
                        val action =
                            NavGraphDirections.actionGlobalAddUpdateFragment(
                                event.tracker,
                                title = getString(R.string.fragment_title_edit_tracker)
                            )
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }
}

package com.example.android.interviewdiary.fragments.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.AbsListView.OnScrollListener.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.android.interviewdiary.ADD_UPDATE_REQUEST
import com.example.android.interviewdiary.NavGraphDirections
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.FragmentHomeBinding
import com.example.android.interviewdiary.other.Constants.BUFFER_PAST
import com.example.android.interviewdiary.other.utils.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import javax.inject.Inject

// TODO Add Tab Bar for Tracker Groups
// TODO Add sorting functionality
// TODO Add select multiple trackers on onLongClick() (e.g. for delete multiple trackers simultaneously)

@AndroidEntryPoint
class HomeFragment @Inject constructor(
    private val homeListAdapter: HomeListAdapter,
    private val homeDateAdapter: HomeDateAdapter
) : Fragment(R.layout.fragment_home) {

    private val homeViewModel: HomeViewModel by viewModels(ownerProducer = { this })

    private lateinit var binding: FragmentHomeBinding

    private val createTrackerDialog = HomeCreateTrackerDialog()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        setHasOptionsMenu(true)
        setBindingObject()
        setDateAdapter()
        setListAdapter()
        setEventListener()

        setFragmentResultListener(ADD_UPDATE_REQUEST) { _, bundle ->
            val result = bundle.getInt(ADD_UPDATE_REQUEST)
            homeViewModel.onAddEditResult(result)
        }
    }

    private fun setBindingObject() {
        binding.apply {

            rvHome.apply {
                adapter = homeListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)

                // TODO Optimize animation
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when (newState) {
                            SCROLL_STATE_TOUCH_SCROLL -> binding.controlBarHome.viewGroup
                                .animate()
                                .translationYBy(binding.controlBarHome.viewGroup.height.toFloat())
                                .apply {
                                    startDelay = 100
                                    duration = 300
                                }
                            SCROLL_STATE_IDLE -> binding.controlBarHome.viewGroup
                                .animate()
                                .translationYBy(-(binding.controlBarHome.viewGroup.y - (binding.root.bottom - binding.controlBarHome.viewGroup.height)))
                                .apply {
                                    startDelay = 300
                                    duration = 500
                                }
                        }
                    }
                })
            }

            controlBarHome.apply {
                btnStartAsking.setOnClickListener {
                    homeViewModel.onStartAskingClick()
                }
                datePicker.apply {
                    btnNextDate.setOnClickListener {
                        binding.controlBarHome.datePicker.vpDate.currentItem++
                    }
                    btnPrevDate.setOnClickListener {
                        binding.controlBarHome.datePicker.vpDate.currentItem--
                    }
                    vpDate.apply {
                        adapter = homeDateAdapter
                        registerOnPageChangeCallback(object :
                            ViewPager2.OnPageChangeCallback() {
                            override fun onPageSelected(position: Int) {
                                homeViewModel.setCurrentDate(position)
                                super.onPageSelected(position)
                            }
                        })
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.btn_menu_export_csv -> {
                homeViewModel.exportToCSVClicked(requireContext())
                true
            }

            R.id.btn_menu_add_tracker -> {
                homeViewModel.onAddButtonClick()
                true
            }

            R.id.btn_menu_add_example_trackers -> {
                homeViewModel.onAddExampleTrackersClick(requireContext())
                true
            }

            R.id.btn_menu_add_example_records -> {
                homeViewModel.onAddExampleRecordsClick()
                true
            }
            R.id.btn_menu_clear_all_trackers -> {
                val builder = AlertDialog.Builder(context)
                builder.setTitle(getString(R.string.dialog_title_clear_trackers))
                builder.setMessage(getString(R.string.dialog_confirm_clear_trackers))
                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    homeViewModel.onClearAllTrackersClick()
                }
                builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
                builder.create().show()
                true
            }
            R.id.btn_backup -> {
                homeViewModel.onBackupClick()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setDateAdapter() {
        homeDateAdapter.apply {
            setOnTvClickListener {
                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { _, year, monthOfYear, dayOfMonth ->
                        homeViewModel.setInitDate(
                            LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                        )
                    },
                    LocalDate.now().year,
                    LocalDate.now().monthValue - 1,
                    LocalDate.now().dayOfMonth
                )
                datePickerDialog.show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            homeViewModel.dateList.collect {
                homeDateAdapter.setDateList(it)
                binding.controlBarHome.datePicker.vpDate.setCurrentItem(
                    BUFFER_PAST.toInt(),
                    false
                )
            }
        }
    }

    private fun setListAdapter() {
        val callback = HomeItemTouchHelper(homeListAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)

        itemTouchHelper.attachToRecyclerView(binding.rvHome)

        homeListAdapter.apply {
            setOnItemLongClickListener { tracker ->
                val builder = AlertDialog.Builder(context)
                builder.setTitle(getString(R.string.dialog_title_delete_tracker))
                builder.setMessage(getString(R.string.dialog_confirm_delete_tracker))
                builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    homeViewModel.onItemLongClick(tracker)
                }
                builder.setNegativeButton(getString(R.string.no)) { _, _ -> }
                builder.create().show()
            }

            setOnSwipeLeftListener { tracker ->
                homeViewModel.onItemSwipeRight(tracker)
            }

            setOnSwipeRightListener { tracker ->
                homeViewModel.onItemSwipeLeft(tracker)
            }

            setOnItemClickListener { tracker ->
                homeViewModel.onItemClick(tracker)
            }

            setOnAddButtonClickListener {
                homeViewModel.onAddButtonClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            homeViewModel.itemList().collect {
                homeListAdapter.createAndSubmitList(it)
            }
        }
    }

    private fun setEventListener() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            homeViewModel.event.collect { event ->
                when (event) {
                    is HomeViewModel.Event.ShowInvalidInputMessage ->
                        Snackbar.make(
                            requireView(),
                            when (event.snackBar) {
                                InvalidInputSnackBar.PLEASE_ADD_A_TRACKER -> getString(R.string.snack_bar_no_trackers_created_yet)
                                InvalidInputSnackBar.PLEASE_ADD_A_RECORD -> getString(R.string.snack_bar_no_record_created_yet)
                                InvalidInputSnackBar.PLEASE_SELECT_AN_ANSWER_TYPE -> getString(R.string.home_dialog_snack_bar_invalid_input_no_answer_type_selected)
                                InvalidInputSnackBar.PLEASE_ENTER_A_QUESTION -> getString(R.string.home_dialog_snack_bar_invalid_input_no_question_entered)
                                InvalidInputSnackBar.PLEASE_CHOOSE_A_SHORTER_QUESTION -> getString(R.string.home_dialog_snack_bar_invalid_input_question_too_long)
                            }.exhaustive, Snackbar.LENGTH_SHORT
                        ).show()

                    is HomeViewModel.Event.ShowConfirmationMessage ->
                        Snackbar.make(
                            requireView(),
                            when (event.snackBar) {
                                ConfMsgSnackBar.TRACKER_ADDED -> getString(R.string.snack_bar_confirm_msg_tracker_created)
                                ConfMsgSnackBar.TRACKER_DELETED -> getString(R.string.snack_bar_confirm_msg_tracker_deleted)
                                ConfMsgSnackBar.TRACKER_UPDATED -> getString(R.string.snack_bar_confirm_msg_tracker_updated)
                                ConfMsgSnackBar.ALL_TRACKERS_CLEARED -> getString(R.string.snack_bar_confirm_msg_all_trackers_cleared)
                            }.exhaustive, Snackbar.LENGTH_SHORT
                        ).show()

                    is HomeViewModel.Event.OpenCreateTrackerDialog ->
                        createTrackerDialog.show(childFragmentManager, "CreateTrackerDialog")

                    is HomeViewModel.Event.NavigateToAddTrackerFragment -> {
                        val action =
                            NavGraphDirections.actionGlobalAddUpdateFragment(
                                question = event.question,
                                trackerType = event.trackerType,
                                title = getString(R.string.fragment_title_add_new_tracker)
                            )
                        findNavController().navigate(action)
                    }

                    is HomeViewModel.Event.NavigateToEditTrackerFragment -> {
                        val action =
                            NavGraphDirections.actionGlobalAddUpdateFragment(
                                tracker = event.tracker,
                                title = getString(R.string.fragment_title_edit_tracker)
                            )
                        findNavController().navigate(action)
                    }

                    is HomeViewModel.Event.NavigateToRecordListFragment -> {
                        val action =
                            NavGraphDirections.actionGlobalRecordListFragment(
                                trackerId = event.trackerId, date = event.date.toString()
                            )
                        findNavController().navigate(action)
                    }

                    is HomeViewModel.Event.NavigateToInterviewFragment -> {
                        val action =
                            NavGraphDirections.actionGlobalInterviewNestedNavGraph(
                                event.trackerIds,
                                event.date
                            )
                        findNavController().navigate(action)
                    }
                    HomeViewModel.Event.NavigateToBackupFragment -> {
                        val action =
                            NavGraphDirections.actionGlobalBackupFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }


}






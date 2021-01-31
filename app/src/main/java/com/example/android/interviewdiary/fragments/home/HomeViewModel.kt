package com.example.android.interviewdiary.fragments.home

import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.android.interviewdiary.ADD_TRACKER_RESULT_OK
import com.example.android.interviewdiary.DELETE_TRACKER_RESULT_OK
import com.example.android.interviewdiary.EDIT_TRACKER_RESULT_OK
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.model.TrackerType
import com.example.android.interviewdiary.other.Constants.BUFFER_FUTURE
import com.example.android.interviewdiary.other.Constants.BUFFER_PAST
import com.example.android.interviewdiary.other.Constants.QUESTION_MAX_LENGTH
import com.example.android.interviewdiary.other.utils.ExportCSVUtils.exportRecordsToCSVFile
import com.example.android.interviewdiary.other.utils.InitDatabaseUtils
import com.example.android.interviewdiary.repositories.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities

enum class InvalidInputSnackBar {
    PLEASE_ADD_A_TRACKER,
    PLEASE_ADD_A_RECORD,
    PLEASE_SELECT_AN_ANSWER_TYPE,
    PLEASE_ENTER_A_QUESTION,
    PLEASE_CHOOSE_A_SHORTER_QUESTION
}

enum class ConfMsgSnackBar {
    TRACKER_ADDED,
    TRACKER_DELETED,
    TRACKER_UPDATED,
    ALL_TRACKERS_CLEARED
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: AppRepository
) : ViewModel() {


    /**
     * Either today or the date, which was chosen in the date picker dialog by the user
     */
    private val initDate = MutableStateFlow<LocalDate>(LocalDate.now())
    fun setInitDate(date: LocalDate) {
        initDate.value = date
        currentDate.value = date
    }

    /**
     * the date, which should be displayed on the UI
     */
    private val currentDate = MutableStateFlow(initDate.value)

    fun setCurrentDate(position: Int) {
        currentDate.value = initDate.value.plusDays(position.toLong() - BUFFER_PAST)
    }

    /**
     * all dates, which can be reached via pressing the next/prev buttons in the control bar
     */
    val dateList = initDate.flatMapLatest { date ->
        val result = ArrayList<LocalDate>()
        for (dif in BUFFER_PAST downTo 1) result.add(date.minusDays(dif))
        result.add(date)
        for (dif in 1..BUFFER_FUTURE) result.add(date.plusDays(dif))
        flowOf(result)
    }

    private fun streamCurrentTrackerIds() = repo.streamAllTrackerIds()

    init {
//        viewModelScope.launch {
//            currentTrackerIds = repo.streamAllTrackerIds()
//        }
    }

    private fun streamTrackers() =
        streamCurrentTrackerIds().flatMapLatest {
            repo.streamTrackers(it)
        }

    private fun streamLatestRecords() =
        combine(streamCurrentTrackerIds(), currentDate) { trackerIds, currentDate ->
            trackerIds to currentDate
        }.flatMapLatest { (trackerIds, currentDate) ->
            repo.streamPastRecords(trackerIds, currentDate).flatMapLatest {records ->
                flowOf(records.groupBy { it?.trackerId }.map { (trackerId, record) -> trackerId to record.maxByOrNull { it!!.date }})
            }
        }

    fun itemList() = combine(
        streamTrackers(),
        streamLatestRecords()
    ) { trackers, latestRecords ->
        trackers to latestRecords
    }.flatMapLatest { (trackers, latestRecords) ->
        flowOf(
            trackers.map { tracker ->
                HomeListAdapter.Item.Tracker(
                    tracker!!,
                    latestRecords.find {tracker.trackerId == it.first }?.second
                )
            }
        )
    }

    private val eventChannel = Channel<Event>()
    val event = eventChannel.receiveAsFlow()

    sealed class Event {
        data class ShowConfirmationMessage(val snackBar: ConfMsgSnackBar) : Event()
        data class ShowInvalidInputMessage(val snackBar: InvalidInputSnackBar) : Event()
        object OpenCreateTrackerDialog : Event()
        object NavigateToBackupFragment : Event()

        data class NavigateToRecordListFragment(
            val trackerId: Int,
            val date: LocalDate
        ) : Event()

        data class NavigateToAddTrackerFragment(
            val question: String? = null,
            val trackerType: TrackerType? = null
        ) : Event()

        data class NavigateToEditTrackerFragment(
            val tracker: Tracker? = null,
        ) : Event()

        data class NavigateToInterviewFragment(
            val trackerIds: IntArray,
            val date: String
        ) : Event()
    }

    fun onItemClick(tracker: Tracker) = navigateToRecordListFragment(tracker)

    fun onItemSwipeLeft(tracker: Tracker) = startInterView(listOf(tracker), currentDate.value)

    fun onItemSwipeRight(tracker: Tracker) = editTracker(tracker)

    fun onItemLongClick(tracker: Tracker) = deleteTracker(tracker)

    fun onAddButtonClick() = openCreateTrackerDialog()

    fun onClearAllTrackersClick() = clearTrackers()

    fun onAddExampleRecordsClick() = addExampleRecords()

    fun onAddExampleTrackersClick(context: Context) = addExampleTrackers(context)

    fun onStartAskingClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val trackers = getAllTracker()
            if (trackers.isEmpty())
                showInvalidInputMessage(InvalidInputSnackBar.PLEASE_ADD_A_TRACKER)
            else startInterView(trackers, currentDate.value)
        }
    }

    fun onCreateTrackerConfirmClick(
        question: String,
        trackerType: TrackerType?
    ): Boolean {
        return if (validateInput(question, trackerType)) {
            createTracker(question = question, trackerType = trackerType!!)
            true
        } else
            false
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TRACKER_RESULT_OK -> showConfirmationMessage(
                ConfMsgSnackBar.TRACKER_ADDED
            )
            EDIT_TRACKER_RESULT_OK -> showConfirmationMessage(
                ConfMsgSnackBar.TRACKER_UPDATED
            )
            DELETE_TRACKER_RESULT_OK -> showConfirmationMessage(
                ConfMsgSnackBar.TRACKER_DELETED
            )
        }
    }

    private fun navigateToRecordListFragment(tracker: Tracker) = viewModelScope.launch {
        eventChannel.send(
            Event.NavigateToRecordListFragment(
                tracker.trackerId,
                currentDate.value
            )
        )
    }

    private fun startInterView(trackers: List<Tracker>, date: LocalDate) =
        viewModelScope.launch {
            eventChannel.send(
                Event.NavigateToInterviewFragment(
                    trackers.map { it.trackerId }.toIntArray(),
                    date.toString()
                )
            )
        }

    private fun openCreateTrackerDialog() = viewModelScope.launch {
        eventChannel.send(Event.OpenCreateTrackerDialog)
    }

    private fun createTracker(question: String, trackerType: TrackerType) =
        viewModelScope.launch {
            eventChannel.send(
                Event.NavigateToAddTrackerFragment(
                    question = question,
                    trackerType = trackerType
                )
            )
        }

    private fun editTracker(tracker: Tracker) = viewModelScope.launch {
        eventChannel.send(Event.NavigateToEditTrackerFragment(tracker = tracker))
    }

    private suspend fun getAllTracker(): List<Tracker> {
        return repo.getAllTrackers()
    }

    private fun deleteTracker(tracker: Tracker) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteTracker(tracker)
        showConfirmationMessage(ConfMsgSnackBar.TRACKER_DELETED)
    }

    private fun clearTrackers() = viewModelScope.launch(Dispatchers.IO) {
        repo.clearAllTrackers()
        showConfirmationMessage(
            ConfMsgSnackBar.ALL_TRACKERS_CLEARED
        )
    }

    private fun showInvalidInputMessage(snackBar: InvalidInputSnackBar) =
        viewModelScope.launch {
            eventChannel.send(Event.ShowInvalidInputMessage(snackBar))
        }

    private fun showConfirmationMessage(snackBar: ConfMsgSnackBar) =
        viewModelScope.launch {
            eventChannel.send(Event.ShowConfirmationMessage(snackBar))
        }

    private fun validateInput(input: String, trackerType: TrackerType?): Boolean {
        return when {
            input.isEmpty() -> {
                showInvalidInputMessage(InvalidInputSnackBar.PLEASE_ENTER_A_QUESTION)
                false
            }
            input.length > QUESTION_MAX_LENGTH -> {
                showInvalidInputMessage(InvalidInputSnackBar.PLEASE_CHOOSE_A_SHORTER_QUESTION)
                false
            }
            trackerType == null -> {
                showInvalidInputMessage(InvalidInputSnackBar.PLEASE_SELECT_AN_ANSWER_TYPE)
                false
            }
            else -> true
        }
    }

    private fun addExampleRecords() = viewModelScope.launch(Dispatchers.IO) {
        val trackers = repo.getAllTrackers()
        if (trackers.isEmpty())
            eventChannel.send(
                Event.ShowInvalidInputMessage(InvalidInputSnackBar.PLEASE_ADD_A_TRACKER)
            )
        else
            InitDatabaseUtils.createInitRecords(trackers, LocalDate.now().minusDays(1), 30)
                .forEach { record ->
                    repo.insertRecord(record)
                }
    }


    private fun addExampleTrackers(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val initTrackers = InitDatabaseUtils.createInitTrackers(context)
            initTrackers.forEach { tracker ->
                repo.insertTracker(tracker)
            }
        }
    }

    fun exportToCSVClicked(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val trackers = repo.getAllTrackers()
            val records = repo.getAllRecords()
            when {
                trackers.isEmpty() -> showInvalidInputMessage(InvalidInputSnackBar.PLEASE_ADD_A_TRACKER)
                records.isEmpty() -> showInvalidInputMessage(InvalidInputSnackBar.PLEASE_ADD_A_RECORD)
                else -> exportRecordsToCSVFile(context, trackers, records)
            }
        }
    }

    fun onBackupClick() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateToBackupFragment)
        }

    }
}

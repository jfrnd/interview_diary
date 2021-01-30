package com.example.android.interviewdiary.fragments.recordList

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.android.interviewdiary.other.utils.ConverterUtil.toDisplayedString
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.repositories.DefaultAppRepository
import com.example.android.interviewdiary.other.Constants.BUFFER_FUTURE
import com.example.android.interviewdiary.other.Constants.BUFFER_PAST
import com.example.android.interviewdiary.other.Constants.DATE
import com.example.android.interviewdiary.other.Constants.TRACKER
import com.example.android.interviewdiary.other.utils.ConverterUtil.toLocalDate
import com.example.android.interviewdiary.other.utils.ExportCSVUtils
import com.example.android.interviewdiary.repositories.AppRepository
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject


enum class MySnackBars {NO_RECORDS_CREATED_YET, RECORDS_CLEARED}

@HiltViewModel
class RecordListViewModel @Inject constructor(
    private val repo: AppRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    val tracker = state.get<Tracker>(TRACKER)

    private var focusedDate = state.get<String>(DATE)!!.toLocalDate()
        set(value) {
            field = value
            state.set(DATE, value)
        }

    private val records = repo.streamAllRecords(tracker!!.trackerId)

    private val eventChannel = Channel<Event>()
    val event = eventChannel.receiveAsFlow()

    fun recordList(): Flow<Pair<ArrayList<RecordListAdapter.Item>, Int>> {
        val result = ArrayList<RecordListAdapter.Item>()
        var focusedPosition = 1

        return records.flatMapLatest { records ->
            result.clear()
            createPeriod(focusedDate).forEachIndexed { index, date ->
                val record = records.find { it.date == date }
                if (date == focusedDate)
                    focusedPosition = index
                if (record != null) {
                    result.add(
                        RecordListAdapter.Item.Record(
                            record.recordId,
                            record.values,
                            record.date,
                            record.note,
                            date == focusedDate
                        )
                    )
                } else
                    result.add(
                        RecordListAdapter.Item.Date(
                            date = date,
                            date == focusedDate
                        )
                    )
            }
            flowOf(result to focusedPosition)
        }
    }

    fun onItemClick(date: LocalDate) {
        focusedDate = date
        viewModelScope.launch {
            eventChannel.send(Event.NavigateToEditRecord(date))
        }
    }

    fun onEditTrackerClick() {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateToEditTracker(tracker!!))
        }
    }

    fun onUndoClick(record: Record) {
        addRecord(record)
    }

    fun onClearRecordsClick() {
        clearRecords()
        showGenericSnackBar(MySnackBars.RECORDS_CLEARED)
    }

    private fun showGenericSnackBar(snackBar: MySnackBars) = viewModelScope.launch {
        eventChannel.send(Event.ShowGenericSnackBar(snackBar))
    }

    private fun clearRecords() {
        viewModelScope.launch {
            repo.clearRecords(tracker!!.trackerId)
        }
    }

    private fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repo.deleteRecord(record)
        }
    }

    private fun addRecord(record: Record) {
        viewModelScope.launch {
            repo.insertRecord(record)
        }
    }

    fun onItemSwipe(recordId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = repo.getRecord(recordId)
            deleteRecord(record!!)
            eventChannel.send(Event.ShowRecordDeletedSnackBar(record))
        }
    }


    fun exportToCSVClicked(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val records = repo.getAllRecords(tracker!!.trackerId)
            when {
                records.isEmpty() -> showGenericSnackBar(MySnackBars.NO_RECORDS_CREATED_YET)
                else -> ExportCSVUtils.exportRecordsToCSVFile(context, listOf(tracker), records)
            }
        }
    }


    sealed class Event {
        data class NavigateToEditRecord(val date: LocalDate) : Event()
        data class NavigateToEditTracker(val tracker: Tracker?) : Event()
        data class ShowRecordDeletedSnackBar(val record: Record) : Event()
        data class ShowGenericSnackBar(val snackBar: MySnackBars) : Event()
    }

    private fun createPeriod(
        focusedDate: LocalDate,
        bufferPast: Long = BUFFER_PAST,
        bufferFuture: Long = BUFFER_FUTURE
    ): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        val endDate = focusedDate.minusDays(bufferPast)
        var currentDate = focusedDate.plusDays(bufferFuture)
        while (currentDate.isAfter(endDate)) {
            result.add(currentDate)
            currentDate = currentDate.minusDays(1)
        }
        return result.toList()
    }


}

package com.example.android.interviewdiary.fragments.interview

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.other.utils.ConverterUtil.toLocalDate
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.model.TrackerType
import com.example.android.interviewdiary.repositories.DefaultAppRepository
import com.example.android.interviewdiary.other.Constants.DATE
import com.example.android.interviewdiary.other.Constants.TRACKER
import com.example.android.interviewdiary.other.Constants.TRACKER_IDS
import com.example.android.interviewdiary.repositories.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

/**
 * Will be instantiated once in the beginning of an interview session.
 */
@HiltViewModel
class InterviewNavigationViewModel @Inject constructor(
    private val repo: AppRepository,
    private val glide: RequestManager,
    state: SavedStateHandle
) : ViewModel() {

    val date: LocalDate = state.get<String>(DATE)!!.toLocalDate()
    private val trackerIDs = state.get<IntArray>(TRACKER_IDS)!!

    var forceNoteInputInCurrentSession: Boolean = true
        private set

    fun isSingleQuestion() = trackerIDs.size == 1
    fun isFirstQuestion() = pointer == 0

    private var pointer = 0

    private val eventChannel = Channel<NavigationEvent>()
    val event = eventChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val newFragment = createNewFragment(trackerIDs[pointer], date)
            eventChannel.send(NavigationEvent.LoadNewQuestion(newFragment))
        }
    }

    fun onEditTrackerClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val tracker = repo.getTracker(trackerIDs[pointer])
            withContext(Dispatchers.Main) {
                eventChannel.send(NavigationEvent.NavigateToEditTracker(tracker!!))
            }
        }
    }

    fun onEditNoteDialogConfirmClick(stopForce: Boolean) {
        forceNoteInputInCurrentSession = !stopForce
        navigateForward()
    }

    fun onNoteClick() {
        openNoteDialogManually()
    }

    fun forceOpenNoteDialog() {
        viewModelScope.launch {
            eventChannel.send(NavigationEvent.OpenNoteDialog(true))
        }
    }

    private fun openNoteDialogManually() {
        viewModelScope.launch {
            eventChannel.send(NavigationEvent.OpenNoteDialog(false))
        }
    }

    fun navigateForward() {
        if (pointer == trackerIDs.size - 1)
            navigateHome()
        else
            navigateToNextQuestion()
    }

    fun navigateBack() {
        if (pointer == 0)
            navigateHome()
        else
            navigateToPrevQuestion()
    }

    private fun navigateHome() {
        viewModelScope.launch {
            eventChannel.send(NavigationEvent.NavigateHome)
        }
    }

    private fun navigateToNextQuestion() {
        pointer += 1
        viewModelScope.launch {
            val headerFragment = createNewFragment(trackerIDs[pointer], date)
            eventChannel.send(
                NavigationEvent.LoadNewQuestion(
                    headerFragment,
                    Animation.NEXT
                )
            )
        }
    }

    private fun navigateToPrevQuestion() {
        pointer -= 1
        viewModelScope.launch {
            val headerFragment = createNewFragment(trackerIDs[pointer], date)
            eventChannel.send(
                NavigationEvent.LoadNewQuestion(
                    headerFragment,
                    Animation.BACK
                )
            )
        }
    }

    private suspend fun createNewFragment(trackerID: Int, date: LocalDate): Fragment {
        val tracker = withContext(Dispatchers.IO) {
            repo.getTracker(trackerID)
        }

        val bundle = Bundle().apply {
            putParcelable(TRACKER, tracker)
            putString(DATE, date.toString())
        }

        val newFragment: Fragment = when (tracker!!.type) {
            TrackerType.MULTIPLE_CHOICE -> InterviewMultipleChoiceFragment(glide)
            TrackerType.NUMERIC -> InterviewNumericFragment(glide)
            TrackerType.TIME -> InterviewTimeFragment(glide)
            TrackerType.YES_NO -> InterviewYesNoFragment(glide)
        }

        return newFragment.apply {
            arguments = bundle
        }
    }


    sealed class NavigationEvent {
        data class LoadNewQuestion(
            val newFragment: Fragment,
            val animation: Animation? = null
        ) : NavigationEvent()

        data class OpenNoteDialog(val forced: Boolean) : NavigationEvent()
        class NavigateToEditTracker(val tracker: Tracker) : NavigationEvent()
        object NavigateHome : NavigationEvent()
    }

    enum class Animation { NEXT, BACK }
}

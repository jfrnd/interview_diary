package com.example.android.interviewdiary.fragments.addEdit

import androidx.lifecycle.*
import com.example.android.interviewdiary.ADD_TRACKER_RESULT_OK
import com.example.android.interviewdiary.DELETE_TRACKER_RESULT_OK
import com.example.android.interviewdiary.EDIT_TRACKER_RESULT_OK
import com.example.android.interviewdiary.model.*
import com.example.android.interviewdiary.repositories.AppRepository
import com.example.android.interviewdiary.other.Constants.ANSWER_OPTIONS
import com.example.android.interviewdiary.other.Constants.ANSWER_OPTION_MAX_LENGTH
import com.example.android.interviewdiary.other.Constants.ANSWER_OPTION
import com.example.android.interviewdiary.other.Constants.CONFIG_VALUES
import com.example.android.interviewdiary.other.Constants.DEFAULT_VALUE
import com.example.android.interviewdiary.other.Constants.ENABLED_FEATURES
import com.example.android.interviewdiary.other.Constants.IMAGE_URI_STRING
import com.example.android.interviewdiary.other.Constants.MAX_VALUE
import com.example.android.interviewdiary.other.Constants.MIN_VALUE
import com.example.android.interviewdiary.other.Constants.NUMERIC_VALUE_MAX_LENGTH
import com.example.android.interviewdiary.other.Constants.QUESTION
import com.example.android.interviewdiary.other.Constants.QUESTION_MAX_LENGTH
import com.example.android.interviewdiary.other.Constants.FRAGMENT_TITLE
import com.example.android.interviewdiary.other.Constants.TRACKER
import com.example.android.interviewdiary.other.Constants.TRACKER_TYPE
import com.example.android.interviewdiary.other.Constants.UNIT
import com.example.android.interviewdiary.other.Constants.UNIT_MAX_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InvalidInputSnackBar {
    STILL_LOADING_DATA,
    FIELD_CANNOT_BE_EMPTY,
    INPUT_MUST_BE_SHORTER,
    USED_INVALID_CHARACTER,
    CREATE_MORE_ANSWER_OPTIONS,
    CHOOSE_UNIQUE_ANSWER_OPTIONS,
    MAX_VAL_SMALLER_THAN_DEF_VAL,
    MAX_VAL_SMALLER_THAN_MIN_VAL,
    DEF_VAL_SMALLER_THAN_MIN_VAL,
    EMPTY_FIELDS,
}

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repo: AppRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    val title = state.get<String>(FRAGMENT_TITLE)
    val tracker = state.get<Tracker>(TRACKER)

    private val trackerType: TrackerType =
        state.get<TrackerType>(TRACKER_TYPE) ?: tracker?.type ?: TrackerType.TIME

    private var imageUriString =
        MutableStateFlow(state.get(IMAGE_URI_STRING) ?: tracker?.imageUri ?: "")
        set(value) {
            field = value
            state.set(IMAGE_URI_STRING, value)
        }

    fun changeImageUri(uriString: String) {
        imageUriString.value = uriString
        state.set(IMAGE_URI_STRING, imageUriString.value)
    }

    private var question =
        MutableStateFlow(state.get<String>(QUESTION) ?: tracker?.question ?: "")

    private fun updateQuestion(input: String) {
        question.value = input.capitalize()
        state.set(QUESTION, question.value)
    }

    private var enabledFeatures =
        state.get<List<Feature>>(ENABLED_FEATURES) ?: tracker?.enabledFeatures
        ?: listOf(Feature.NOTES)


    fun updateSwitchValue(switchType: Int, isChecked: Boolean) {
        when (switchType) {
            SWITCH_TYPE_NOTE_INPUT -> {
                enabledFeatures =
                    if (isChecked)
                        enabledFeatures.plus(Feature.NOTES).distinct()
                    else
                        enabledFeatures.minus(Feature.NOTES).distinct()
                state.set(ENABLED_FEATURES, enabledFeatures)
            }
            SWITCH_TYPE_MULTI_SELECTION -> {
                enabledFeatures =
                    if (isChecked)
                        enabledFeatures.plus(Feature.MULTI_SELECTION).distinct()
                    else
                        enabledFeatures.minus(Feature.MULTI_SELECTION).distinct()
                state.set(ENABLED_FEATURES, enabledFeatures)
            }
            SWITCH_TYPE_DECIMAL -> {
                enabledFeatures =
                    if (isChecked)
                        enabledFeatures.plus(Feature.DECIMAL).distinct()
                    else
                        enabledFeatures.minus(Feature.DECIMAL).distinct()
                state.set(ENABLED_FEATURES, enabledFeatures)
            }
        }
    }

    private var configValues = MutableStateFlow(
        state.get<List<Float>>(CONFIG_VALUES) ?: tracker?.configValues
        ?: when (trackerType) {
            TrackerType.MULTIPLE_CHOICE -> listOf(1f)
            TrackerType.NUMERIC -> listOf(65f, 60f, 70f)
            TrackerType.TIME -> listOf(0f, 0f, 0f)
            TrackerType.YES_NO -> listOf(0f, 1f)
        }
    )

    private fun updateConfigValues(index: Int, newAnswerId: Float) {
        configValues.value = configValues.value.mapIndexed { idx, oldVal ->
            if (idx == index) newAnswerId else oldVal
        }
        state.set(CONFIG_VALUES, configValues.value)
    }

    private fun updateConfigValues(newList: List<Float>) {
        configValues.value = newList
        state.set(CONFIG_VALUES, configValues.value)
    }

    var unit = MutableStateFlow(state.get<String>(UNIT) ?: tracker?.unit ?: "")

    private fun updateUnit(input: String) {
        unit.value = input
        state.set(UNIT, unit.value)
    }

    var answerOptions = MutableStateFlow(
        state.get<Map<Int, String>>(ANSWER_OPTIONS) ?: tracker?.answerOptions ?: mapOf(1 to "")
    )

    private fun updateAnswerOption(answerId: Float, value: String?) {
        if (value == null)
            answerOptions.value = answerOptions.value.minus(answerId.toInt())
        else
            answerOptions.value = answerOptions.value.plus(answerId.toInt() to value.capitalize())
        state.set(ANSWER_OPTIONS, answerOptions.value)
    }

    private var allRecords: List<Record>? = null
    private var _allRecordValues: List<Float>? = null
    val allRecordValues: List<Float>?
        get() = _allRecordValues


    // Preload records in the background, in order to avoid loading time after onSaveClick()
    init {
        if (tracker != null && trackerType == TrackerType.MULTIPLE_CHOICE)
            viewModelScope.launch(Dispatchers.IO) {
                allRecords = repo.getAllRecords(tracker.trackerId)
                _allRecordValues = allRecords!!.map { it.values }.flatten()
            }
    }

    private val eventChannel = Channel<Event>()
    val event = eventChannel.receiveAsFlow()

    sealed class Event {
        object OpenPickImageDialog : Event()
        data class OpenEditTextDialog(val itemViewType: Int, val index: Int, val answerId: Int) :
            Event()

        data class OpenEraseRecordEntriesDialog(val answerIds: List<Float>) : Event()
        data class ShowInvalidInputMessage(val snackBar: InvalidInputSnackBar) : Event()
        data class NavigateBackWithResult(val result: Int) : Event()
    }

    // Init Adapter
    fun createAdapterItemList(): Flow<List<AddEditAdapter.Item>> {
        val genericItems = createGenericItems()
        val specificItems = when (trackerType) {
            TrackerType.MULTIPLE_CHOICE -> createMCItems()
            TrackerType.NUMERIC -> createNumericItems()
            TrackerType.TIME -> createTimePickerItems()
            TrackerType.YES_NO -> flowOf(listOf())
        }
        return combine(genericItems, specificItems) { genericItems, specificItems ->
            genericItems + specificItems
        }
    }

    private fun createGenericItems(): Flow<List<AddEditAdapter.Item>> {
        return combine(imageUriString, question) { imageUri, question ->
            imageUri to question
        }.flatMapLatest { (imageUri, question) ->
            flowOf(
                listOf(
                    AddEditAdapter.Item.Image(imageUri),
                    AddEditAdapter.Item.Question(question),
                    AddEditAdapter.Item.Switch(
                        SWITCH_NOTE_INPUT_ADAPTER_POSITION,
                        enabledFeatures.contains(Feature.NOTES),
                        SWITCH_TYPE_NOTE_INPUT
                    )
                )
            )
        }
    }

    private fun createTimePickerItems(): Flow<List<AddEditAdapter.Item>> {
        return flowOf(
            listOf(
                AddEditAdapter.Item.TimePicker(
                    hh = configValues.value[0].toInt(),
                    mm = configValues.value[1].toInt(),
                    ss = configValues.value[2].toInt()
                )
            )
        )
    }

    private fun createNumericItems(): Flow<List<AddEditAdapter.Item>> {
        return combine(configValues, unit) { configValues, unit ->
            (configValues to unit)
        }.flatMapLatest { (configValues, unit) ->
            flowOf(
                listOf(
                    AddEditAdapter.Item.Switch(
                        SWITCH_DECIMAL_ADAPTER_POSITION,
                        enabledFeatures.contains(Feature.DECIMAL),
                        SWITCH_TYPE_DECIMAL
                    ),
                    AddEditAdapter.Item.Numeric(0, configValues[0].toInt().toString()),
                    AddEditAdapter.Item.Numeric(1, configValues[1].toInt().toString()),
                    AddEditAdapter.Item.Numeric(2, configValues[2].toInt().toString()),
                    AddEditAdapter.Item.Unit(unit)
                )
            )
        }
    }

    private fun createMCItems(): Flow<List<AddEditAdapter.Item>> {
        return combine(
            configValues,
            answerOptions
        ) { configValues, answerOptions ->
            configValues to answerOptions
        }.flatMapLatest { (configValues, answerOptions) ->
            flowOf(
                listOf(
                    AddEditAdapter.Item.Switch(
                        SWITCH_MULTI_SELECTION_ADAPTER_POSITION,
                        enabledFeatures.contains(Feature.MULTI_SELECTION),
                        SWITCH_TYPE_MULTI_SELECTION
                    ), AddEditAdapter.Item.AnswerOptionHeader
                ) + configValues.mapIndexed { index, answerId ->
                    AddEditAdapter.Item.AnswerOption(
                        answerId = answerId.toInt(),
                        text = (answerOptions)[answerId.toInt()] ?: "",
                        position = index + 1,
                        hasDuplicate = answerId.toInt().hasDuplicate(answerOptions)
                    )
                } + listOf(AddEditAdapter.Item.AddButton)
            )
        }
    }


    private fun List<Float>?.dataNotFetchedYet(): Boolean = this == null

    private fun List<Float>.containDeletedAnswerOptions(): Boolean {
        val deletedAnswerOptions =
            tracker!!.answerOptions.keys.filter { !answerOptions.value.keys.contains(it) }
        return this.any { deletedAnswerOptions.contains(it) }
    }

    private fun List<Float>.filterDeletedAnswerOptions(): List<Float> {
        val deletedAnswerOptions =
            tracker!!.answerOptions.keys.filter { !answerOptions.value.keys.contains(it) }
        return this.filter { deletedAnswerOptions.contains(it) }.distinct()
    }

    private fun createUpdateTracker() {
        when {
            tracker == null -> createNewTracker()
            trackerType != TrackerType.MULTIPLE_CHOICE -> updateTracker()
            _allRecordValues.dataNotFetchedYet() -> showInvalidInputMessage(
                InvalidInputSnackBar.STILL_LOADING_DATA
            )
            // This scenario is probably very rare but has to be considered:
            // The user (accidentally) deletes and answer option which belongs to existing records
            // In this case these records have to be updated as well
            _allRecordValues!!.containDeletedAnswerOptions() -> viewModelScope.launch {
                eventChannel.send(
                    Event.OpenEraseRecordEntriesDialog(
                        answerIds = _allRecordValues!!.filterDeletedAnswerOptions(),
                    )
                )
            }
            else -> updateTracker()
        }
    }

    fun onSaveClick() {
        if (validateData(trackerType)) {
            createUpdateTracker()
        }
    }

    fun onDeleteClick(tracker: Tracker) {
        deleteTracker(tracker)
    }

    fun onItemClick(itemViewType: Int, index: Int?, answerId: Int?) {
        when (itemViewType) {
            VIEW_TYPE_ADD_EDIT_IMAGE -> openPickImageDialog()
            VIEW_TYPE_ADD_EDIT_QUESTION -> openEditTextDialog(itemViewType)
            VIEW_TYPE_ADD_EDIT_UNIT -> openEditTextDialog(itemViewType)
            VIEW_TYPE_ADD_EDIT_NUMERIC -> openEditTextDialog(itemViewType, index)
            VIEW_TYPE_ADD_EDIT_ANSWER_OPTION -> openEditTextDialog(itemViewType, null, answerId)
        }
    }

    fun onAddButtonClick() {
        addNewAnswerOption()
    }

    fun onTimePickerValueChange(idx: Int, value: Int) = updateConfigValues(idx, value.toFloat())

    fun onItemMoveStart() = initTempConfigValues(configValues.value)

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        tempConfigValues = tempConfigValues.swap(
            fromPosition - ANSWER_OPTIONS_STARTING_ADAPTER_POSITION,
            toPosition - ANSWER_OPTIONS_STARTING_ADAPTER_POSITION
        )
    }

    fun onItemMoveFinish() = updateConfigValues(tempConfigValues)

    fun onAnswerRemoveClick(answerId: Float) = removeAnswerOption(answerId)

    fun onRetrieveAnswerOptionsClick(deletedAnswerIds: IntArray) =
        retrieveAnswerOptions(deletedAnswerIds)

    fun onEraseRecordEntriesClick(deletedAnswerIds: IntArray) =
        viewModelScope.launch(Dispatchers.IO) {
            eraseRecordEntries(deletedAnswerIds)
            updateTracker()
        }

    fun onEditTextConfirmClick(
        input: String,
        itemViewType: Int,
        index: Int,
        answerId: Int
    ): Boolean {
        return when (itemViewType) {
            VIEW_TYPE_ADD_EDIT_QUESTION ->
                if (validateTextInput(input, QUESTION_MAX_LENGTH)) {
                    updateQuestion(input)
                    true
                } else false
            VIEW_TYPE_ADD_EDIT_ANSWER_OPTION ->
                if (validateTextInput(input, ANSWER_OPTION_MAX_LENGTH)) {
                    updateAnswerOption(answerId.toFloat(), input)
                    true
                } else false
            VIEW_TYPE_ADD_EDIT_UNIT ->
                if (validateTextInput(input, UNIT_MAX_LENGTH, 0)) {
                    updateUnit(input)
                    true
                } else false
            VIEW_TYPE_ADD_EDIT_NUMERIC ->
                if (validateTextInput(input, NUMERIC_VALUE_MAX_LENGTH)) {
                    updateConfigValues(index, input.toFloat())
                    true
                } else false
            else -> throw ClassCastException("Unknown ViewType $itemViewType")
        }
    }

    private fun openPickImageDialog() = viewModelScope.launch {
        eventChannel.send(Event.OpenPickImageDialog)
    }

    private fun openEditTextDialog(itemViewType: Int, index: Int? = null, answerId: Int? = null) =
        viewModelScope.launch {
            eventChannel.send(
                Event.OpenEditTextDialog(
                    itemViewType = itemViewType,
                    index = index ?: 0,
                    answerId = answerId ?: 0
                )
            )
        }

    // Pair of "Headline Label" and "Body Value"
    fun initEditTextDialog(itemViewType: Int, index: Int, answerId: Int): Pair<String, String> {
        return when (itemViewType) {
            VIEW_TYPE_ADD_EDIT_QUESTION -> QUESTION to question.value
            VIEW_TYPE_ADD_EDIT_ANSWER_OPTION -> ANSWER_OPTION to answerOptions.value[answerId]!!
            VIEW_TYPE_ADD_EDIT_UNIT -> UNIT to unit.value
            VIEW_TYPE_ADD_EDIT_NUMERIC -> when (index) {
                0 -> DEFAULT_VALUE to configValues.value[0].toString()
                1 -> MIN_VALUE to configValues.value[1].toString()
                2 -> MAX_VALUE to configValues.value[2].toString()
                else -> throw ClassCastException("Index $index of ViewType $VIEW_TYPE_ADD_EDIT_NUMERIC doesn't exist")
            }
            else -> throw ClassCastException("Unknown ViewType $itemViewType")
        }
    }

    private fun validateTextInput(
        input: String,
        maxLength: Int,
        minLength: Int = 1
    ): Boolean {
        return when {
            input.length < minLength -> {
                showInvalidInputMessage(InvalidInputSnackBar.FIELD_CANNOT_BE_EMPTY)
                false
            }
            input.length > maxLength -> {
                showInvalidInputMessage(InvalidInputSnackBar.INPUT_MUST_BE_SHORTER)
                false
            }
            input.contains(';') -> {
                showInvalidInputMessage(InvalidInputSnackBar.USED_INVALID_CHARACTER)
                false
            }
            else -> true
        }
    }

    private fun createNewTracker() {
        val newTracker = Tracker(
            question = question.value,
            imageUri = imageUriString.value,
            answerOptions = answerOptions.value,
            configValues = configValues.value,
            unit = unit.value,
            type = trackerType,
            enabledFeatures = enabledFeatures
        )
        viewModelScope.launch {
            repo.insertTracker(newTracker)
            navigateBackWithResult(ADD_TRACKER_RESULT_OK)
        }
    }

    private fun updateTracker() = viewModelScope.launch(Dispatchers.IO) {
        val updatedTracker =
            tracker!!.copy(
                question = question.value,
                imageUri = imageUriString.value,
                answerOptions = answerOptions.value,
                configValues = configValues.value,
                unit = unit.value,
                type = trackerType,
                enabledFeatures = enabledFeatures
            )
        repo.updateTracker(updatedTracker)
        navigateBackWithResult(EDIT_TRACKER_RESULT_OK)
    }


    private fun deleteTracker(tracker: Tracker) = viewModelScope.launch {
        repo.deleteTracker(tracker)
        eventChannel.send(Event.NavigateBackWithResult(DELETE_TRACKER_RESULT_OK))
    }

    private fun addNewAnswerOption() {
        val newId = getNewAnswerId(tracker?.configValues, configValues.value)
        updateConfigValues(configValues.value.plus(newId))
        updateAnswerOption(newId, "")
    }

    private fun removeAnswerOption(answerId: Float) {
        updateAnswerOption(answerId, null)
        updateConfigValues(configValues.value.minus(answerId))
    }

    private suspend fun eraseRecordEntries(answerIdsToBeErased: IntArray) {
        allRecords!!.filter { record ->
            record.values.any { answerId ->
                answerIdsToBeErased.contains(answerId.toInt())
            }
        }
            .forEach { record ->
                val updatedRecord = record.copy(
                    values = record.values.filter { !answerIdsToBeErased.contains(it.toInt()) }
                )
                if (updatedRecord.values.isEmpty())
                    repo.deleteRecord(updatedRecord)
                else
                    repo.updateRecord(updatedRecord)
            }
    }

    private fun retrieveAnswerOptions(delAnswerIds: IntArray) {
        delAnswerIds.forEach { answerId ->
            updateAnswerOption(answerId.toFloat(), tracker!!.answerOptions[answerId])
            updateConfigValues(configValues.value + listOf(answerId.toFloat()))
        }
    }

    private var tempConfigValues: List<Float> = emptyList()
    private fun initTempConfigValues(configValues: List<Float>) {
        tempConfigValues = configValues
    }

    private fun validateData(trackerType: TrackerType)
            : Boolean {
        return when (trackerType) {
            TrackerType.TIME -> true
            TrackerType.YES_NO -> true
            TrackerType.MULTIPLE_CHOICE -> {
                validateDataMultipleChoice(configValues.value, answerOptions.value)
            }
            TrackerType.NUMERIC -> {
                validateDataNumeric(configValues.value)
            }
        }
    }

    private fun validateDataMultipleChoice(
        configValues: List<Float>,
        answerOptions: Map<Int, String>
    ): Boolean {
        return when {
            configValues.size <= 1 -> {
                showInvalidInputMessage(InvalidInputSnackBar.CREATE_MORE_ANSWER_OPTIONS)
                false
            }
            answerOptions.values.distinct().size < answerOptions.values.size -> {
                showInvalidInputMessage(InvalidInputSnackBar.CHOOSE_UNIQUE_ANSWER_OPTIONS)
                false
            }
            answerOptions.values.any { it.isBlank() } -> {
                showInvalidInputMessage(InvalidInputSnackBar.EMPTY_FIELDS)
                false
            }
            else -> true
        }
    }

    private fun validateDataNumeric(configValues: List<Float>): Boolean {
        val defaultValue = configValues[0]
        val minValue = configValues[1]
        val maxValue = configValues[2]
        return when {
            minValue >= maxValue -> {
                showInvalidInputMessage(InvalidInputSnackBar.MAX_VAL_SMALLER_THAN_MIN_VAL)
                false
            }
            minValue > defaultValue -> {
                showInvalidInputMessage(InvalidInputSnackBar.DEF_VAL_SMALLER_THAN_MIN_VAL)
                false
            }
            maxValue < defaultValue -> {
                showInvalidInputMessage(InvalidInputSnackBar.MAX_VAL_SMALLER_THAN_DEF_VAL)
                false
            }
            else -> return true
        }
    }

    private fun navigateBackWithResult(result: Int) {
        viewModelScope.launch {
            eventChannel.send(Event.NavigateBackWithResult(result))
        }
    }

    private fun showInvalidInputMessage(snackBar: InvalidInputSnackBar) = viewModelScope.launch {
        eventChannel.send(Event.ShowInvalidInputMessage(snackBar))
    }

    private fun getNewAnswerId(
        inputOriginalIds: List<Float>?,
        currentIds: List<Float>
    ): Float {
        val originalIds = inputOriginalIds ?: emptyList()
        val blockedIds = (currentIds + originalIds + listOf(0f)).distinct()
        return blockedIds.first { !blockedIds.contains(it + 1f) } + 1f
    }

    private fun Int.hasDuplicate(answerOptions: Map<Int, String>) =
        answerOptions.count { it.value.isNotBlank() && it.value == answerOptions[this] } > 1


    private fun List<Float>.swap(index1: Int, index2: Int)
            : List<Float> {
        val result = this.toMutableList()
        val val1 = this[index1]
        val val2 = this[index2]
        result[index1] = val2
        result[index2] = val1
        return result.toList()
    }


}







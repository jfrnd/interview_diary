package com.example.android.interviewdiary.fragments.addEdit

import android.annotation.SuppressLint
import android.net.Uri
import android.view.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

//VIEW TYPES
const val VIEW_TYPE_ADD_EDIT_IMAGE = 0
const val VIEW_TYPE_ADD_EDIT_QUESTION = 1
const val VIEW_TYPE_ADD_EDIT_SWITCH = 2
const val VIEW_TYPE_ADD_EDIT_TIME_PICKER = 3
const val VIEW_TYPE_ADD_EDIT_NUMERIC = 4
const val VIEW_TYPE_ADD_EDIT_UNIT = 5
const val VIEW_TYPE_ADD_EDIT_ANSWER_OPTION = 6
const val VIEW_TYPE_ADD_EDIT_ADD_BUTTON = 7
const val VIEW_TYPE_ADD_EDIT_ANSWER_OPTION_HEADER = 8

//Adapter Positions
//Header
const val IMAGE_ADAPTER_POSITION = 0
const val QUESTION_ADAPTER_POSITION = 1
const val SWITCH_NOTE_INPUT_ADAPTER_POSITION = 2

//Time Picker
const val TIME_PICKER_ADAPTER_POSITION = 3

//Numeric
const val DEFAULT_VALUE_ADAPTER_POSITION = 3
const val MIN_VALUE_ADAPTER_POSITION = 4
const val MAX_VALUE_ADAPTER_POSITION = 5
const val UNIT_ADAPTER_POSITION = 6

//MC
const val SWITCH_MULTI_SELECTION_ADAPTER_POSITION = 3
const val ANSWER_OPTIONS_TITLE_ADAPTER_POSITION = 4
const val ANSWER_OPTIONS_STARTING_ADAPTER_POSITION = 5

class AddEditAdapter @Inject constructor(
    private val glide: RequestManager,
) :
    ListAdapter<AddEditAdapter.Item, RecyclerView.ViewHolder>(DiffCallback()),
    AddEditItemTouchHelperAdapter {

    private var itemTouchHelper: ItemTouchHelper? = null

    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun createAndSubmitList(list: List<Item>) {
        adapterScope.launch {
            val items = ArrayList<Item>()
            list.forEach { item ->
                when (item) {
                    is Item.Image -> items.add(item as Item.Image)
                    is Item.Question -> items.add(item as Item.Question)
                    is Item.Switch -> items.add(item as Item.Switch)
                    is Item.TimePicker -> items.add(item as Item.TimePicker)
                    is Item.Unit -> items.add(item as Item.Unit)
                    is Item.Numeric -> items.add(item as Item.Numeric)
                    is Item.AnswerOptionHeader -> items.add(item as Item.AnswerOptionHeader)
                    is Item.AnswerOption -> items.add(item as Item.AnswerOption)
                    is Item.AddButton -> items.add(item as Item.AddButton)
                }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.Image -> VIEW_TYPE_ADD_EDIT_IMAGE
            is Item.Question -> VIEW_TYPE_ADD_EDIT_QUESTION
            is Item.Switch -> VIEW_TYPE_ADD_EDIT_SWITCH
            is Item.TimePicker -> VIEW_TYPE_ADD_EDIT_TIME_PICKER
            is Item.Unit -> VIEW_TYPE_ADD_EDIT_UNIT
            is Item.Numeric -> VIEW_TYPE_ADD_EDIT_NUMERIC
            is Item.AnswerOption -> VIEW_TYPE_ADD_EDIT_ANSWER_OPTION
            is Item.AddButton -> VIEW_TYPE_ADD_EDIT_ADD_BUTTON
            is Item.AnswerOptionHeader -> VIEW_TYPE_ADD_EDIT_ANSWER_OPTION_HEADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val bindingImage =
            ItemAddEditImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        val bindingQuestion =
            ItemAddEditQuestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        val bindingSwitch =
            ItemAddEditSwitchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        val bindingTimePicker =
            ItemAddEditTimePickerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        val bindingNumeric =
            ItemAddEditNumericValueBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        val bindingAnswerOption =
            ItemAddEditAnswerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val bindingAnswerOptionHeader =
            ItemAddEditAnswerListHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        val bindingAddButton =
            ItemAddEditAddButtonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return when (viewType) {
            VIEW_TYPE_ADD_EDIT_IMAGE -> ImageViewHolder(bindingImage)
            VIEW_TYPE_ADD_EDIT_QUESTION -> QuestionViewHolder(bindingQuestion)
            VIEW_TYPE_ADD_EDIT_SWITCH -> SwitchViewHolder(bindingSwitch)
            VIEW_TYPE_ADD_EDIT_TIME_PICKER -> TimePickerViewHolder(bindingTimePicker)
            VIEW_TYPE_ADD_EDIT_NUMERIC -> NumericViewHolder(bindingNumeric)
            VIEW_TYPE_ADD_EDIT_UNIT -> UnitViewHolder(bindingNumeric)
            VIEW_TYPE_ADD_EDIT_ANSWER_OPTION -> AnswerOptionViewHolder(bindingAnswerOption)
            VIEW_TYPE_ADD_EDIT_ADD_BUTTON -> AddButtonViewHolder(bindingAddButton)
            VIEW_TYPE_ADD_EDIT_ANSWER_OPTION_HEADER -> AnswerOptionHeaderViewHolder(
                bindingAnswerOptionHeader
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> {
                val currentItem = getItem(position) as Item.Image
                holder.bind(currentItem)
            }
            is QuestionViewHolder -> {
                val currentItem = getItem(position) as Item.Question
                holder.bind(currentItem)
            }
            is SwitchViewHolder -> {
                val currentItem = getItem(position) as Item.Switch
                holder.bind(currentItem)
            }
            is TimePickerViewHolder -> {
                val currentItem = getItem(position) as Item.TimePicker
                holder.bind(currentItem)
            }
            is NumericViewHolder -> {
                val currentItem = getItem(position) as Item.Numeric
                holder.bind(currentItem)
            }
            is UnitViewHolder -> {
                val currentItem = getItem(position) as Item.Unit
                holder.bind(currentItem)
            }
            is AnswerOptionViewHolder -> {
                val currentItem = getItem(position) as Item.AnswerOption
                holder.bind(currentItem)
            }
            is AddButtonViewHolder -> return
            is AnswerOptionHeaderViewHolder -> return
        }
    }

    inner class ImageViewHolder(
        private val binding: ItemAddEditImageBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick?.let { click ->
                    click(itemViewType, null, null)
                }
            }
        }

        fun bind(
            currentItem: Item.Image,
        ) {
            glide.load(Uri.parse(currentItem.imageUri)).into(binding.
            ivAddEdit)
        }
    }

    inner class QuestionViewHolder(
        private val binding: ItemAddEditQuestionBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick?.let { click ->
                    click(itemViewType, null, null)
                }
            }
        }

        fun bind(
            currentItem: Item.Question,
        ) {
            binding.tvHeadline.text = currentItem.question
        }
    }

    inner class SwitchViewHolder(
        private val binding: ItemAddEditSwitchBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.switchAddEdit.setOnCheckedChangeListener { _, isChecked ->
                onSwitchChanged?.let { check ->
                    check(adapterPosition, isChecked)
                }
            }
        }

        fun bind(
            currentItem: Item.Switch,
        ) {
            binding.root.setOnClickListener {
                binding.switchAddEdit.isChecked = !binding.switchAddEdit.isChecked
            }

            binding.switchAddEdit.isChecked = currentItem.value
            when (adapterPosition) {
                SWITCH_NOTE_INPUT_ADAPTER_POSITION -> {
                    binding.tvHeadline.text = itemView.context.resources.getString(R.string.add_edit_switch_add_notes_headline)
                    binding.tvBody.text =
                        itemView.context.resources.getString(R.string.add_edit_switch_add_notes_body)
                    binding.icSwitch.setImageDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.ic_note
                        )
                    )

                }
                SWITCH_MULTI_SELECTION_ADAPTER_POSITION -> {
                    binding.tvHeadline.text =
                        itemView.context.resources.getString(R.string.add_edit_switch_multi_selection_headline)
                    binding.tvBody.text =
                        itemView.context.resources.getString(R.string.add_edit_switch_multi_selection_body)
                    binding.icSwitch.setImageDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.ic_check_box
                        )
                    )
                }
            }
        }
    }

    inner class TimePickerViewHolder(
        private val binding: ItemAddEditTimePickerBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                npHh.apply {
                    setFormatter { value ->
                        value.toString().padStart(2, '0')
                    }
                    setOnValueChangedListener { picker, _, _ ->
                        onTimePickerValueChange?.let { change ->
                            change(0, picker.value)
                        }
                    }
                }
                npMm.apply {
                    setFormatter { value ->
                        value.toString().padStart(2, '0')
                    }
                    setOnValueChangedListener { picker, _, _ ->
                        onTimePickerValueChange?.let { change ->
                            change(1, picker.value)
                        }
                    }
                }
                npSs.apply {
                    setFormatter { value ->
                        value.toString().padStart(2, '0')
                    }
                    setOnValueChangedListener { picker, _, _ ->
                        onTimePickerValueChange?.let { change ->
                            change(2, picker.value)
                        }
                    }
                }
            }
        }

        fun bind(
            currentItem: Item.TimePicker,
        ) {
            binding.apply {
                npHh.value = currentItem.hh
                npMm.value = currentItem.mm
                npSs.value = currentItem.ss
            }
        }
    }

    inner class UnitViewHolder(
        private val binding: ItemAddEditNumericValueBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            currentItem: Item.Unit,
        ) {

            binding.apply {
                tvHeadline.text = itemView.context.resources.getString(R.string.add_edit_unit_headline)
                tvBody.text = itemView.context.resources.getString(R.string.add_edit_unit_body)
                tvValue.text = currentItem.value
                tvValue.hint =
                    if (currentItem.value.isBlank()) itemView.context.resources.getString(R.string.add_edit_unit_example) else currentItem.value
                root.setOnClickListener {
                    onItemClick?.let { click ->
                        click(itemViewType, null, null)
                    }
                }
            }
        }
    }

    inner class NumericViewHolder(
        private val binding: ItemAddEditNumericValueBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            currentItem: Item.Numeric,
        ) {
            val labelHeadline = when (currentItem.index) {
                0 -> itemView.context.resources.getString(R.string.add_edit_default_value_headline)
                1 -> itemView.context.resources.getString(R.string.add_edit_min_value_headline)
                2 -> itemView.context.resources.getString(R.string.add_edit_max_value_headline)
                else -> ""
            }
            val labelBody = when (currentItem.index) {
                0 -> itemView.context.resources.getString(R.string.add_edit_default_value_body)
                1 -> itemView.context.resources.getString(R.string.add_edit_min_value_body)
                2 -> itemView.context.resources.getString(R.string.add_edit_max_value_body)
                else -> ""
            }

            binding.apply {
                tvHeadline.text = labelHeadline
                tvBody.text = labelBody
                tvValue.text = currentItem.value
                root.setOnClickListener {
                    onItemClick?.let { click ->
                        click(itemViewType, currentItem.index, null)
                    }
                }
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    inner class AnswerOptionViewHolder(
        private val binding: ItemAddEditAnswerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.icMove.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_MOVE && adapterPosition != RecyclerView.NO_POSITION) {
                    itemTouchHelper?.startDrag(this@AnswerOptionViewHolder)
                    onItemMoveStart?.let { trigger ->
                        trigger()
                    }
                }
                true
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(currentItem: Item.AnswerOption) {

            binding.apply {
                tvHeadline.text = currentItem.text
                tvNumber.text = (currentItem.position).toString() + ":"
                icDelete.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onAnswerDeleteClick?.let { click ->
                            click(currentItem.answerId)
                            notifyItemRemoved(adapterPosition)
                        }
                    }
                }

                root.apply {
                    setOnClickListener {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            onItemClick?.let { click ->
                                click(itemViewType, null, currentItem.answerId)
                            }
                        }
                    }
                    setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            if (currentItem.hasDuplicate)
                                R.color.light_orange
                            else
                                R.color.white
                        )
                    )
                }
            }

        }
    }

    inner class AddButtonViewHolder(binding: ItemAddEditAddButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnAdd.setOnClickListener {
                onAddButtonClick?.let { click ->
                    click()
                }
            }
        }
    }

    inner class AnswerOptionHeaderViewHolder(binding: ItemAddEditAnswerListHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)



    private var onItemClick: ((Int, Int?, Int?) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int, Int?, Int?) -> Unit) {
        onItemClick = listener
    }


    private var onSwitchChanged: ((Int, Boolean) -> Unit)? = null

    fun setOnSwitchCheckedListener(listener: (Int, Boolean) -> Unit) {
        onSwitchChanged = listener
    }

    private var onTimePickerValueChange: ((Int, Int) -> Unit)? = null

    fun setOnTimePickerValueChangeListener(listener: (Int, Int) -> Unit) {
        onTimePickerValueChange = listener
    }

    private var onAddButtonClick: (() -> Unit)? = null

    fun setOnAddButtonClickListener(listener: () -> Unit) {
        onAddButtonClick = listener
    }

    private var onAnswerDeleteClick: ((Int) -> Unit)? = null

    fun setOnAnswerDeleteClick(listener: (Int) -> Unit) {
        onAnswerDeleteClick = listener
    }

    private var onItemMoveStart: (() -> Unit)? = null

    fun setOnItemMoveStartListener(listener: () -> Unit) {
        onItemMoveStart = listener
    }

    private var onItemMove: ((Int, Int) -> Unit)? = null

    fun setOnItemMoveListener(listener: (Int, Int) -> Unit) {
        onItemMove = listener
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
        onItemMove?.let { move ->
            move(fromPosition, toPosition)
        }
    }

    private var onItemMoveFinish: (() -> Unit)? = null

    fun setOnItemMoveFinishListener(listener: () -> Unit) {
        onItemMoveFinish = listener
    }

    override fun onItemMoveFinish() {
        onItemMoveFinish?.let { trigger ->
            trigger()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(
            oldItem: Item,
            newItem: Item
        ): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Item,
            newItem: Item
        ): Boolean = oldItem == newItem
    }

    sealed class Item {

        data class Image(
            val imageUri: String,
        ) : Item() {
            override val id = IMAGE_ADAPTER_POSITION.toLong()
        }

        data class Question(
            val question: String,
        ) : Item() {
            override val id = QUESTION_ADAPTER_POSITION.toLong()
        }

        data class Switch(
            val position: Int,
            val value: Boolean
        ) : Item() {
            override val id = position.toLong()
        }

        data class Unit(
            val value: String
        ) : Item() {
            override val id = UNIT_ADAPTER_POSITION.toLong()
        }

        data class Numeric(
            val index: Int,
            val value: String
        ) : Item() {
            override val id = index.toLong() + DEFAULT_VALUE_ADAPTER_POSITION
        }

        data class TimePicker(
            val hh: Int,
            val mm: Int,
            val ss: Int,
        ) : Item() {
            override val id = TIME_PICKER_ADAPTER_POSITION.toLong()
        }

        data class AnswerOption(
            val answerId: Int,
            val text: String,
            val position: Int,
            val hasDuplicate: Boolean
        ) :
            Item() {
            override val id = position.toLong() + ANSWER_OPTIONS_STARTING_ADAPTER_POSITION
        }

        object AnswerOptionHeader : Item() {
            override val id = ANSWER_OPTIONS_TITLE_ADAPTER_POSITION.toLong()
        }

        object AddButton : Item() {
            override val id = Long.MAX_VALUE
        }

        abstract val id: Long
    }


}



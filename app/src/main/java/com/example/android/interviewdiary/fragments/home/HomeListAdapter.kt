package com.example.android.interviewdiary.fragments.home


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.other.utils.ConverterUtil.toDisplayedString
import com.example.android.interviewdiary.databinding.ItemHomeAddTrackerButtonBinding
import com.example.android.interviewdiary.databinding.ItemHomeTrackerBinding
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.Tracker
import javax.inject.Inject

const val ITEM_VIEW_TYPE_TRACKER = 0
const val ITEM_VIEW_TYPE_ADD = 1

class HomeListAdapter @Inject constructor(
    private val glide: RequestManager,
) :
    ListAdapter<HomeListAdapter.Item, RecyclerView.ViewHolder>(DiffCallback()),
    HomeItemTouchHelperAdapter {

    fun createAndSubmitList(items: List<Item.Tracker>?) {
        if (items == null)
            submitList(listOf(Item.AddButton))
        else
            submitList(items + listOf(Item.AddButton))
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.AddButton -> ITEM_VIEW_TYPE_ADD
            is Item.Tracker -> ITEM_VIEW_TYPE_TRACKER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val bindingTracker =
            ItemHomeTrackerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val bindingAddButton =
            ItemHomeAddTrackerButtonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return when (viewType) {
            ITEM_VIEW_TYPE_ADD -> AddButtonViewHolder(bindingAddButton)
            ITEM_VIEW_TYPE_TRACKER -> TrackerViewHolder(bindingTracker)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TrackerViewHolder -> {
                val currentItem = getItem(position) as Item.Tracker
                holder.bind(currentItem)
            }
            is AddButtonViewHolder -> return
        }
    }

    inner class TrackerViewHolder(
        private val binding: ItemHomeTrackerBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            currentItem: Item.Tracker,
        ) {
            val currentTracker = currentItem.tracker
            val currentRecord = currentItem.record
            val currentDay = currentRecord?.date

            binding.root.setOnClickListener {
                onItemClickListener?.let { click ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION)
                        click(currentTracker)
                }
            }

            binding.root.setOnLongClickListener {
                onItemLongClickListener?.let { longClick ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        longClick(currentTracker)
                    }
                }
                true
            }

            binding.apply {
                tvQuestion.text = currentTracker.question
                tvAnswer.text = currentRecord?.values?.toDisplayedString(currentItem.tracker)
                    ?: itemView.context.getString(R.string.home_item_not_answered)
                tvDate.text = currentDay.toDisplayedString(false,itemView.context)
                glide.load(currentTracker.imageUri).into(ivHome)
            }
        }
    }

    inner class AddButtonViewHolder(binding: ItemHomeAddTrackerButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnAdd.setOnClickListener {
                onAddButtonClickListener?.let { click ->
                    click()
                }
            }
        }
    }

    private var onAddButtonClickListener: (() -> Unit)? = null

    fun setOnAddButtonClickListener(listener: () -> Unit) {
        onAddButtonClickListener = listener
    }

    private var onItemLongClickListener: ((Tracker) -> Unit)? = null

    fun setOnItemLongClickListener(listener: (Tracker) -> Unit) {
        onItemLongClickListener = listener
    }

    private var onItemClickListener: ((Tracker) -> Unit)? = null

    fun setOnItemClickListener(listener: (Tracker) -> Unit) {
        onItemClickListener = listener
    }

    private var onItemSwipeLeftListener: ((Tracker) -> Unit)? = null

    fun setOnSwipeLeftListener(listener: (Tracker) -> Unit) {
        onItemSwipeLeftListener = listener
    }

    override fun onItemSwipeLeft(adapterPosition: Int) {
        onItemSwipeLeftListener?.let { swipe ->
            swipe((getItem(adapterPosition) as Item.Tracker).tracker)
        }
    }

    private var onItemSwipeRightListener: ((Tracker) -> Unit)? = null

    fun setOnSwipeRightListener(listener: (Tracker) -> Unit) {
        onItemSwipeRightListener = listener
    }

    override fun onItemSwipeRight(adapterPosition: Int) {
        onItemSwipeRightListener?.let { swipe ->
            swipe((getItem(adapterPosition) as Item.Tracker).tracker)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean = oldItem == newItem
    }

    sealed class Item {

        data class Tracker(
            val tracker: com.example.android.interviewdiary.model.Tracker,
            val record: Record?
        ) : Item() {
            override val id = tracker.trackerId.toLong()
        }

        object AddButton : Item() {
            override val id = Long.MAX_VALUE
        }

        abstract val id: Long
    }


}

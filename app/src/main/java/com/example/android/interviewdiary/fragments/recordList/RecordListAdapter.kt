package com.example.android.interviewdiary.fragments.recordList

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.ItemRecordListDateBinding
import com.example.android.interviewdiary.databinding.ItemRecordListRecordBinding
import com.example.android.interviewdiary.other.utils.ConverterUtil.toDisplayedString
import com.example.android.interviewdiary.other.utils.ConverterUtil.differenceToToday
import java.time.LocalDate
import javax.inject.Inject

const val ITEM_VIEW_TYPE_RECORD_LIST_RECORD = 0
const val ITEM_VIEW_TYPE_RECORD_LIST_DATE = 1

class RecordListAdapter @Inject constructor(
) :
    ListAdapter<RecordListAdapter.Item, RecyclerView.ViewHolder>(DiffCallback()),
    RecordListItemTouchHelperAdapter {

    fun createAndSubmitList(itemList: ArrayList<Item>) {
        val items = ArrayList<Item>()
        itemList.forEach {
            if (it is Item.Date) items.add(it)
            else items.add(it as Item.Record)
        }
        submitList(items)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.Record -> ITEM_VIEW_TYPE_RECORD_LIST_RECORD
            is Item.Date -> ITEM_VIEW_TYPE_RECORD_LIST_DATE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_RECORD_LIST_RECORD -> {
                val binding = ItemRecordListRecordBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RecordViewHolder(binding)
            }
            ITEM_VIEW_TYPE_RECORD_LIST_DATE -> {
                val binding = ItemRecordListDateBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DateViewHolder(binding)
            }

            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecordViewHolder -> {
                val currentItem =
                    getItem(position) as Item.Record
                holder.bind(currentItem)
            }
            is DateViewHolder -> {
                val currentItem =
                    getItem(position) as Item.Date
                holder.bind(currentItem)
            }
        }
    }

    inner class DateViewHolder(private val binding: ItemRecordListDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: Item.Date) {

            binding.tvDate.apply {
                text = currentItem.date.toDisplayedString(context = context)
                typeface = if (currentItem.isHighlighted) {
                    setTextColor(ContextCompat.getColor(itemView.context,R.color.primary_color))
                    Typeface.DEFAULT_BOLD
                } else {
                    setTextColor(Color.GRAY)
                    Typeface.DEFAULT
                }
            }

            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION)
                    onItemClickListener?.let { click ->
                        click(currentItem.date)
                    }
            }
        }
    }

    inner class RecordViewHolder(private val binding: ItemRecordListRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currentItem: Item.Record) {
            binding.tvAnswer.text = currentItem.answer
            binding.tvDate.apply {
                text = currentItem.date.toDisplayedString(context = context)
                typeface = if (currentItem.isHighlighted) {
                    setTextColor(ContextCompat.getColor(itemView.context,R.color.primary_color))
                    Typeface.DEFAULT_BOLD
                } else {
                    setTextColor(Color.GRAY)
                    Typeface.DEFAULT
                }
            }
            if (currentItem.note.isBlank()) {
                binding.tvNote.apply {
                    setTypeface(binding.tvNote.typeface, Typeface.ITALIC)
                    text = itemView.context.resources.getString(R.string.record_list_no_note)
                }
            } else
                binding.tvNote.apply {
                    setTypeface(binding.tvNote.typeface, Typeface.NORMAL)
                    text = currentItem.note
                }
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && position != 0)
                    onItemClickListener?.let { click ->
                        click(currentItem.date)
                    }

            }
        }
    }

    private var onItemClickListener: ((LocalDate) -> Unit)? = null

    fun setOnItemClickListener(listener: (LocalDate) -> Unit) {
        onItemClickListener = listener
    }

    private var onItemSwipeListener: ((Int) -> Unit)? = null

    fun setOnSwipeListener(listener: (Int) -> Unit) {
        onItemSwipeListener = listener
    }

    override fun onItemSwipe(position: Int) {
        onItemSwipeListener?.let { swipe ->
            swipe((getItem(position) as Item.Record).recordId!!)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }

    sealed class Item {
        data class Date(
            val date: LocalDate,
            val isHighlighted: Boolean = false
        ) : Item() {
            override val id: Long = date.differenceToToday().toLong() + 1
        }

        data class Record(
            val recordId: Int?,
            val answer: String,
            val date: LocalDate,
            val note: String,
            val isHighlighted: Boolean = false
        ) : Item() {
            override val id = date.differenceToToday().toLong() + 1
        }

        abstract val id: Long

    }
}




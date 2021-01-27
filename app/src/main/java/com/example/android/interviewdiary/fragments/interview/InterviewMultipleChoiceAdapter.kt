package com.example.android.interviewdiary.fragments.interview

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.databinding.ItemInterviewAnswerBinding
import com.example.android.interviewdiary.databinding.ItemInterviewHeaderBinding
import javax.inject.Inject

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ANSWER = 1

class InterviewMultipleChoiceAdapter @Inject constructor(
    private val glide: RequestManager,
) :
    ListAdapter<InterviewMultipleChoiceAdapter.Item, RecyclerView.ViewHolder>(DiffCallback()) {

    fun createAndSubmitList(list: List<Item>) {

        val items: List<Item> = listOf((list[0] as Item.Header)) + list.drop(1).map {
            it as Item.Answer
        }
        submitList(items)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Item.Header -> ITEM_VIEW_TYPE_HEADER
            is Item.Answer -> ITEM_VIEW_TYPE_ANSWER
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val binding = ItemInterviewHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            ITEM_VIEW_TYPE_ANSWER -> {
                val binding = ItemInterviewAnswerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AnswerViewHolder(binding)
            }
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val currentItem =
                    getItem(position) as Item.Header
                holder.bind(currentItem)
            }
            is AnswerViewHolder -> {
                val currentItem =
                    getItem(position) as Item.Answer

                holder.bind(currentItem)
            }
        }
    }

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    private var onNoteClick: (() -> Unit)? = null

    fun setOnNoteClickListener(listener: () -> Unit) {
        onNoteClick = listener
    }

    inner class HeaderViewHolder(private val binding: ItemInterviewHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: Item.Header) {
            binding.tvQuestion.text = currentItem.question
            glide.load(currentItem.imageUrl).into(binding.ivInterview)

            when {
                !currentItem.notesEnabled -> binding.boxNote.visibility =
                    View.GONE
                currentItem.note.isNullOrBlank() -> binding.apply {
                    icEdit.visibility = View.INVISIBLE
                    icAdd.visibility = View.VISIBLE
                    boxNote.setOnClickListener {
                        onNoteClick?.let { click ->
                            click()
                        }
                    }
                }
                else -> binding.apply {
                    icEdit.visibility = View.VISIBLE
                    icAdd.visibility = View.INVISIBLE
                    tvNote.text = currentItem.note
                    boxNote.setOnClickListener {
                        onNoteClick?.let { click ->
                            click()
                        }
                    }
                }
            }
        }
    }

    inner class AnswerViewHolder(private val binding: ItemInterviewAnswerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currentItem: Item.Answer) {
            binding.apply {
                btnInterviewAnswerOption.apply {
                    text = currentItem.text
                    isChecked = currentItem.isSelectedAsCurVal
                    setOnClickListener {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            onItemClickListener?.let { click ->
                                click(currentItem.answerId)
                            }
                        }
                    }
                }
                cbInterviewAnswerOption.apply {
                    cbInterviewAnswerOption.isVisible = currentItem.multiSelectionEnabled
                    isChecked = currentItem.isSelectedAsCurVal
                    setOnClickListener {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            onItemClickListener?.let { click ->
                                click(currentItem.answerId)
                            }
                        }
                    }
                }
                ivSave.apply {
                    isVisible = currentItem.isSelectedInDb
                    ivSave.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            itemView.context,
                            if (currentItem.isSelectedAsCurVal) R.color.secondary_color else R.color.gray
                        )
                    )
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(
            oldItem: Item,
            newItem: Item
        ): Boolean {
            return oldItem.id == oldItem.id
        }

        override fun areContentsTheSame(
            oldItem: Item,
            newItem: Item
        ): Boolean {
            return oldItem == newItem
        }
    }

    sealed class Item {
        data class Header(
            val key: Int = 0,
            val question: String?,
            val imageUrl: String?,
            val note: String?,
            val notesEnabled: Boolean
        ) : Item() {
            override val id = key.toLong()
        }

        data class Answer(
            val multiSelectionEnabled: Boolean,
            val answerId: Int,
            val text: String = "",
            val isSelectedInDb: Boolean = false,
            val isSelectedAsCurVal: Boolean = false
        ) : Item() {

            override val id = answerId.toLong() ?: Long.MAX_VALUE
        }

        abstract val id: Long

    }
}




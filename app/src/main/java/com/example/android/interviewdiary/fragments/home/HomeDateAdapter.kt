package com.example.android.interviewdiary.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.android.interviewdiary.databinding.ItemHomeViewpagerDateBinding
import com.example.android.interviewdiary.other.utils.ConverterUtil.toDisplayedString
import java.time.LocalDate
import javax.inject.Inject

class HomeDateAdapter @Inject constructor() : RecyclerView.Adapter<HomeDateAdapter.ViewHolder>() {

    var dates: ArrayList<LocalDate> = ArrayList()

    private var onTvDateClickListener: (() -> Unit)? = null

    inner class ViewHolder(private val binding: ItemHomeViewpagerDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvDate.setOnClickListener {
                onTvDateClickListener?.let { click ->
                    click()
                }
            }
        }

        fun bind() {
            binding.tvDate.text = dates[adapterPosition].toDisplayedString(true,itemView.context)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemViewpagerDateBinding =
            ItemHomeViewpagerDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemViewpagerDateBinding)
    }

    override fun getItemCount(): Int {
        return dates.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    fun setDateList(dates: ArrayList<LocalDate>) {
        this.dates = dates
        notifyDataSetChanged()
    }

    fun setOnTvClickListener(listener: () -> Unit) {
        onTvDateClickListener = listener
    }
}
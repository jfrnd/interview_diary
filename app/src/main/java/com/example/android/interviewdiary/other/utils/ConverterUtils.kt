package com.example.android.interviewdiary.other.utils

import android.content.Context
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.model.Feature
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.model.TrackerType
import com.example.android.interviewdiary.other.utils.ConverterUtil.toDisplayedString
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private val databaseFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

object ConverterUtil {

    fun String.toLocalDate(): LocalDate = databaseFormatter.parse(this, LocalDate::from)


    fun LocalDate.differenceToToday(): Int {
        val today = LocalDate.now()
        return when {
            this == today -> 0
            this.isBefore(today) -> -1 * Duration.between(this.atStartOfDay(), today.atStartOfDay())
                .toDays().toInt()
            this.isAfter(today) -> Duration.between(this.atStartOfDay(), today.atStartOfDay())
                .toDays().toInt()
            else -> 0
        }

    }

    fun LocalDate?.toDisplayedString(detailed: Boolean = false, context: Context): String {
        val tomorrow = LocalDate.now().plusDays(1)
        val today = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)
        if (!detailed)
            return when (this) {
                null -> ""
                today -> context.resources.getString(R.string.today)
                yesterday -> context.resources.getString(R.string.yesterday)
                tomorrow -> context.resources.getString(R.string.tomorrow)
                else -> "${dayOfMonth.toString().padStart(2, '0')} $month".take(6)
            }
        else
            return when (this) {
                null -> ""
                today -> context.resources.getString(R.string.today) + ", " + "${
                    dayOfMonth.toString().padStart(2, '0')
                } $month".take(6)
                yesterday -> context.resources.getString(R.string.yesterday) + ", " + "${
                    dayOfMonth.toString().padStart(2, '0')
                } $month".take(6)
                tomorrow -> context.resources.getString(R.string.tomorrow) + ", " + "${
                    dayOfMonth.toString().padStart(2, '0')
                } $month".take(6)
                else -> "$dayOfWeek".toLowerCase(Locale.ROOT).capitalize() + ", " + "${
                    dayOfMonth.toString().padStart(2, '0')
                } $month".take(6)
            }
    }

    fun List<Float>.toDisplayedString(tracker: Tracker?, context: Context): String {
        tracker?.let {
            return when (tracker.type) {
                TrackerType.MULTIPLE_CHOICE -> this.map { answerId ->
                    tracker.answerOptions[answerId.toInt()]
                }.joinToString(", ")
                TrackerType.NUMERIC -> {
                    if (tracker.enabledFeatures.contains(Feature.DECIMAL))
                        this.first().round(1).toString() + " " + tracker.unit
                    else
                        this.first().toInt().toString() + " " + tracker.unit
                }
                TrackerType.TIME -> this.joinToString(":") {
                    it.toInt().toString().padStart(2, '0')
                }
                TrackerType.YES_NO -> when (this) {
                    listOf(1f) -> context.getString(R.string.yes)
                    listOf(0f) -> context.getString(R.string.no)
                    else -> ""
                }
            }
        } ?: return ""
    }

    fun List<Float?>.toExcelString(tracker: Tracker, context: Context): List<String> {
        return if (this.isNullOrEmpty() || this.contains(null)) listOf(context.resources.getString(R.string.record_list_empty_record))
        else
            when (tracker.type) {
                TrackerType.MULTIPLE_CHOICE -> this.map { tracker.answerOptions[it?.toInt()]!! }
                TrackerType.NUMERIC -> {
                    if (tracker.enabledFeatures.contains(Feature.DECIMAL))
                        listOf(this.first()?.round(1).toString() + " " + tracker.unit)
                    else
                        listOf(this.first()?.toInt().toString() + " " + tracker.unit)
                }
                TrackerType.TIME -> listOf(this.joinToString(":") {
                    it.toString().padStart(2, '0')
                })
                TrackerType.YES_NO -> when (this) {
                    listOf(1) -> listOf(context.getString(R.string.yes))
                    listOf(0) -> listOf(context.getString(R.string.no))
                    else -> listOf("")
                }
            }
    }

    fun Float.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }


}
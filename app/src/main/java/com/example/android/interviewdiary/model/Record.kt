package com.example.android.interviewdiary.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate




/**
 * Corresponds to "answer item" (German: Antworteintrag) in the UI.
 */
@Parcelize
@Entity(
    tableName = "record_table",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Tracker::class,
            parentColumns = arrayOf("tracker_id"),
            childColumns = arrayOf("tracker_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Record(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id", index = true)
    val recordId: Int = 0,
    @ColumnInfo(name = "tracker_id", index = true)
    val trackerId: Int,
    val date: LocalDate = LocalDate.now(),
    /**
     * Contains the UI user selection for the question at this date.
     *
     * Corresponds to configValues of the [Tracker] data class.
     *
     * Numeric: [0]:= any value between the min and max value
     *
     * Time: [0]:= HH | [1]:= MM | [2]:= SS
     *
     * Multiple Choice: [x]:= answerID of the selected answer options
     */
    val values: List<Int> = listOf(),
    val note: String = ""
) : Parcelable
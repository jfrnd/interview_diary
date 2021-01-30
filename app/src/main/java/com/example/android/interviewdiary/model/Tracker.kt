package com.example.android.interviewdiary.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


/**
 * Corresponds to "question card" (German: Fragekarte) in the UI.
 */
@Parcelize
@Entity(tableName = "tracker_table")
data class Tracker(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tracker_id", index = true)
    val trackerId: Int = 0,
    val imageUri: String,
    val question: String,
//    /**
//     * Not implemented yet.
//     * Tracker groups, to which this tracker belongs to.
//     */
//    val trackerGroup: List<String> = listOf("default group"), // TODO Add tracker groups functionality
    /**
     * Meaning of indices:
     *
     * Numeric: [0]:= default value | [1]:= min value | [2]:= max value
     *
     * Time: default values for [0]:= HH | [1]:= MM | [2]:= SS
     *
     * Multiple Choice: [x]:= answerID of an answer option
     */
    val configValues: List<Int>,
    /**
     * Only relevant for Multiple Choice Trackers.
     * Key: AnswerID (corresponds to the answerIDs in the configValues)
     * Value: The text of the answer itself
     */
    val answerOptions: Map<Int, String> = emptyMap(),
    val type: TrackerType,
    /**
     * Only relevant for Multiple Choice Trackers
     * If true, it is possible to select multiple answer options when creating a new record
     */
    val multiSelectionEnabled: Boolean = false,
    /**
     * Only relevant for Numeric Trackers
     */
    val unit: String = "",
    /**
     * If true, it is possible to add notes when creating a record
     */
    val notesEnabled: Boolean = true,
) : Parcelable

@Parcelize
enum class TrackerType : Parcelable { MULTIPLE_CHOICE, NUMERIC, TIME, YES_NO }
// TODO Add new tracker types: e.g.
//      GPS location (e.g. if you are on a round trip,
//      Text Only input for open questions (e.g. describe your day)
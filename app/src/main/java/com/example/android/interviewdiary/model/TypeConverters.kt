package com.example.android.interviewdiary.model

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class Converters {
    private val databaseFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val databaseFormatter_two: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    fun stringToOffsetDateTime(input: String): OffsetDateTime =
        databaseFormatter_two.parse(input, OffsetDateTime::from)

    @TypeConverter
    fun offsetDateTimeToString(input: OffsetDateTime): String =
        input.toString()



    @TypeConverter
    fun stringToMap(input: String): Map<Int, String> {
        if (input == "") return emptyMap()
        val stringToMapStep1 = input.split(",")
        val keys = stringToMapStep1.map { it.substringBefore('=').trim().toInt() }
        val values = stringToMapStep1.map { it.substringAfter('=') }
        return keys.zip(values).toMap()
    }

    @TypeConverter
    fun mapToString(input: Map<Int, String>): String = input.toString().dropLast(1).drop(1)

    @TypeConverter
    fun stringToLocalDate(input: String): LocalDate =
        databaseFormatter.parse(input, LocalDate::from)

    @TypeConverter
    fun localDateToString(input: LocalDate): String =
        input.format(databaseFormatter)

    @TypeConverter
    fun intToTrackerType(i: Int): TrackerType =
        TrackerType.values()[i]

    @TypeConverter
    fun trackerTypeToInt(featureType: TrackerType): Int =
        featureType.ordinal

    @TypeConverter
    fun intListToString(input: List<Int?>): String {
        if (input.contains(null)) return ""
        return input.joinToString(";")
    }

    @TypeConverter
    fun stringToIntList(input: String): List<Int> {
        if (input == "") return arrayListOf()
        return input.split(";").map { it.toInt() }
    }


}
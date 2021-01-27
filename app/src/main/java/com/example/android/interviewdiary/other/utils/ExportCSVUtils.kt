package com.example.android.interviewdiary.other.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.other.utils.ConverterUtil.toExcelString
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ExportCSVUtils {

    suspend fun exportRecordsToCSVFile(context: Context, trackers: List<Tracker>, records: List<Record>) {

        val allUris: ArrayList<Uri> = arrayListOf()

        trackers.forEach {tracker ->
            val csvFile = File(context.filesDir,"${tracker.question}.csv")
            csvFile.createNewFile()
            csvWriter().open(csvFile,append = false) {
                writeRow(listOf(tracker.question) + listOf("note") + listOf("value"))
                records.filter { it.trackerId == tracker.trackerId }.forEach {record->
                    writeRow(listOf(record.date.toString()) + listOf(record.note) + record.values.toExcelString(tracker, context))
                }
            }
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", csvFile)
            allUris.add(contentUri)
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, allUris)
            type = "text/csv"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        withContext(Dispatchers.Main){
            context.startActivity(shareIntent)
        }
    }
}


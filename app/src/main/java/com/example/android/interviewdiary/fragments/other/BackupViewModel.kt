package com.example.android.interviewdiary.fragments.other

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.android.interviewdiary.other.Constants.DATABASE_NAME
import com.example.android.interviewdiary.repositories.DefaultAppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupViewModel @ViewModelInject constructor(
    private val repo: DefaultAppRepository,
) : ViewModel() {

    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery?, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.checkpoint(supportSQLiteQuery)
            withContext(Dispatchers.Main) {

                val databaseDir = context.getDatabasePath(DATABASE_NAME)

            }
        }
    }
}
package com.example.android.interviewdiary.model

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// TODO Implement Preferences (language, dark theme, sorting orders etc.)

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context)
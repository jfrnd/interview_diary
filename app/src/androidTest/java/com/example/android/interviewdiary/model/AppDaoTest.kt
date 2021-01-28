package com.example.android.interviewdiary.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.android.interviewdiary.other.utils.InitTestDatabaseUtils.createFakeRecords
import com.example.android.interviewdiary.other.utils.InitTestDatabaseUtils.createFakeTrackers
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

@SmallTest
@HiltAndroidTest
class AppDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
    lateinit var database: AppDatabase
    lateinit var dao: AppDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = database.getDao()
        val trackers = createFakeTrackers()
        val records = createFakeRecords(trackers, LocalDate.now(), 30)
        runBlocking {
            trackers.forEach {
                dao.insertTracker(it)
            }
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getTracker() = runBlockingTest {
        val tracker = dao.getTracker(1)
        assertThat(tracker?.trackerId).isEqualTo(1)
    }
}


package com.example.android.interviewdiary.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.android.dx.Local
import com.example.android.interviewdiary.other.utils.InitTestDatabaseUtils.createFakeRecords
import com.example.android.interviewdiary.other.utils.InitTestDatabaseUtils.createFakeTrackers
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.OffsetDateTime
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
        runBlocking {
            trackers.forEach {
                dao.insertTracker(it)
            }
            dao.insertRecord(
                Record(
                    trackerId = 1,
                    recordId = 1,
                    date = LocalDate.now(),
                    values = listOf(1),
                    note = ""
                )
            )
            dao.insertRecord(
                Record(
                    trackerId = 1,
                    recordId = 2,
                    date = LocalDate.now().minusDays(1),
                    values = listOf(),
                    note = ""
                )
            )
            dao.insertRecord(
                Record(
                    trackerId = 1,
                    recordId = 3,
                    date = LocalDate.now().minusDays(2),
                    values = listOf(),
                    note = ""
                )
            )
        }
    }


    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun test () {
        runBlockingTest {
            val record = dao.getRecord(1)
            assertThat(record?.recordId).isEqualTo(1)

        }
    }

//    @Test
//    fun testOrder() {
//        var recordsONE = emptyList<Pair<Int,Int>>()
//        var recordsTWO = emptyList<Pair<Int,Int>>()
//        runBlockingTest {
//            recordsONE = dao.getAllRecords().map { it.recordId to it.date.dayOfMonth }
//            recordsTWO = dao.testOrder().map { it.recordId to it.date.dayOfMonth }
//            val tes = 1
//        }
//
//
//    }

}


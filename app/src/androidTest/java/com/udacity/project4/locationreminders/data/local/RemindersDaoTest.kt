package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@SmallTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private val reminderDTO1 = ReminderDTO("1", "first", "loc", 122.6, 122.7)
    private val reminderDTO2 = ReminderDTO("2", "second", "loc2", 0.0, 0.0)
    private lateinit var database: RemindersDatabase


    @Before
    fun initializationDatabase() {
        database = Room.inMemoryDatabaseBuilder(getApplicationContext(), RemindersDatabase::class.java).allowMainThreadQueries().build()
    }

    @After
    fun close_DataBase() {
        database.close()
    }

    @Test
    fun insertFindByID() =
        runBlockingTest {
            database.reminderDao().saveReminder(reminderDTO1)
            val check = database.reminderDao().getReminderById(reminderDTO1.id)
            assertThat(check as ReminderDTO, notNullValue())
            assertThat(check.latitude, `is`(reminderDTO1.latitude))
            assertThat(check.location, `is`(reminderDTO1.location))
            assertThat(check.id, `is`(reminderDTO1.id))
            assertThat(check.longitude, `is`(reminderDTO1.longitude))
            assertThat(check.description, `is`(reminderDTO1.description))
            assertThat(check.title, `is`(reminderDTO1.title))
        }

    @Test
    fun insert_Reminders_And_FetchAll() = runBlockingTest {
        database.reminderDao().saveReminder(reminderDTO1)
        database.reminderDao().saveReminder(reminderDTO2)
        val reminderDTOList = database.reminderDao().getReminders()
        assertThat(reminderDTOList.isNotEmpty(), `is`(true))
    }

    @Test
    fun deleteAll() = runBlockingTest {
        database.reminderDao().saveReminder(reminderDTO1)
        database.reminderDao().deleteAllReminders()
        assertThat(database.reminderDao().getReminders().isEmpty(), `is`(true))
    }

    @Test
    fun insert_andDelete() = runBlockingTest {
        database.reminderDao().saveReminder(reminderDTO1)
        database.reminderDao().deleteAllReminders()
        assertThat(database.reminderDao().getReminderById(reminderDTO1.id), `is`(nullValue()))
    }
}

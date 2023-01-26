package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {
    private val reminderDTO = ReminderDTO("1", "2", "3", 100.0, 120.0)
    private val reminderDTO1 = ReminderDTO("2", "second", "loc2", 0.0, 0.0)
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun init_dataBase() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersLocalRepository =
            RemindersLocalRepository(remindersDatabase.reminderDao(), TestCoroutineDispatcher())
    }

    @After
    fun close_DataBase() {
        remindersDatabase.close()
    }

    @Test
    fun reminderNotFound() = runBlocking {
        val result = remindersLocalRepository.getReminder(reminderDTO.id)
        assertThat(result is Result.Error, `is`(true))

        result as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))

    }

    @Test
    fun inserting_FindById() = runBlocking {
        remindersLocalRepository.saveReminder(reminderDTO)
        val result =
            remindersLocalRepository.getReminder(reminderDTO.id) as Result.Success<ReminderDTO>
        val loaded = result.data
        assertThat(loaded.longitude, `is`(reminderDTO.longitude))
        assertThat(loaded.latitude, `is`(reminderDTO.latitude))
        assertThat(loaded, CoreMatchers.notNullValue())
        assertThat(loaded.id, `is`(reminderDTO.id))
        assertThat(loaded.description, `is`(reminderDTO.description))
        assertThat(loaded.location, `is`(reminderDTO.location))
        assertThat(loaded.title, `is`(reminderDTO.title))
    }

    @Test
    fun remindersOrNull() = runBlocking {
        remindersDatabase.reminderDao().saveReminder(reminderDTO1)
        remindersDatabase.reminderDao().saveReminder(reminderDTO)
        val result: Result<List<ReminderDTO>> = remindersLocalRepository.getReminders()
        assertThat(result is Result.Success, `is`(true))
        if (result is Result.Success) assertThat(result.data.isNotEmpty(), `is`(true))
    }

    @Test
    fun add_delete_SingleReminder() = runBlocking {
        remindersLocalRepository.saveReminder(reminderDTO)
        remindersLocalRepository.deleteAllReminders()
        assertThat(remindersLocalRepository.getReminder(reminderDTO.id) is Result.Error, `is`(true))
    }

    @Test
    fun deleteAllReminders() = runBlocking {
        remindersLocalRepository.deleteAllReminders()
        val res = remindersLocalRepository.getReminders() as Result.Success
        val dataRes = res.data
        assertThat(dataRes, `is`(emptyList()))
    }
}
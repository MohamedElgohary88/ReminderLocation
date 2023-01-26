package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private var reminderDTOMutableList = mutableListOf<ReminderDTO>()
    private var returnError: Boolean = false
    fun setReturnError(item: Boolean) {
        returnError = item
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (returnError) {
            Result.Error("error")
        } else {
            Result.Success<List<ReminderDTO>>(reminderDTOMutableList)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTOMutableList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return when {
            returnError -> { Result.Error("error") }
            else -> {
                when (val reminder = reminderDTOMutableList.find { it.id == id }) {
                    null -> { Result.Error("Not Found") }
                    else -> { Result.Success(reminder) }
                }
            }
        }
    }

    override suspend fun deleteAllReminders() {
        reminderDTOMutableList.removeAll(reminderDTOMutableList)
    }
}
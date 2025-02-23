package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private var shouldReturnError = false

    var reminderData = mutableListOf<ReminderDTO>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return Result.Success(reminderData)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderData.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        val res = reminderData.find {
            it.id == id
        }
        return if (res == null) {
            Result.Error("Reminder not found!")
        } else {
            Result.Success(res)
        }
    }

    override suspend fun deleteAllReminders() {
        reminderData.clear()
    }


}
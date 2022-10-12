package com.udacity.project4.locationreminders

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source (Done)
    var shouldReturnError = false

    fun adjustReturnErrorOrNot(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
//        TODO("Return the reminders") (Done)
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("Reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
//        TODO("save the reminder") (Done)
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
//        TODO("return the reminder with the id") (Done)
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        val reminder = reminders?.find {
            it.id == id
        }
        return if (reminder != null) {
            Result.Success(reminder)
        } else {
            Result.Error("Reminder not found")
        }
    }

    override suspend fun deleteAllReminders() {
//        TODO("delete all the reminders") (Done)
        reminders?.clear()
    }


}
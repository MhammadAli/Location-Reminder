package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt(Done)

    lateinit var localRepository: RemindersLocalRepository
    lateinit var db: RemindersDatabase

    private val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
    private val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0, "3")
    private val reminder4 = ReminderDTO("Title4", "Description4", "Location4", 4.0, 4.0, "4")


    // Executing each task synchronously by using the architecture components
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDBAndRepository() {
        //We used the in-memory db because the information stored here disappearing when the process's killed
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localRepository = RemindersLocalRepository(db.reminderDao(), Dispatchers.Main)
    }

    //Closing the database
    @After
    fun closingDB() = db.close()

    @Test
    fun savingReminder_gettingReminderByID() = runBlocking {
        //Given - Saving a new reminder in the database
        localRepository.saveReminder(reminder4)

        //When - Getting a reminder by id
        val reminder = localRepository.getReminder(reminder4.id)

        //Then - Same reminder is returned
        reminder as Result.Success
        assertThat(reminder.data.title, `is`(reminder4.title))
        assertThat(reminder.data.description, `is`(reminder4.description))
        assertThat(reminder.data.longitude, `is`(reminder4.longitude))
        assertThat(reminder.data.latitude, `is`(reminder4.latitude))
        assertThat(reminder.data.id, `is`(reminder4.id))
        assertThat(reminder.data.location, `is`(reminder4.location))
    }

    @Test
    fun savingReminders_gettingAllReminders() = runBlocking {
        //Given - Saving new reminders in the database
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)
        localRepository.saveReminder(reminder4)

        //When - Getting all reminders
        val allReminders = localRepository.getReminders()

        //Then - The number of retrieved reminders is correct
        allReminders as Result.Success
        assertThat(allReminders.data.size, `is`(4))
    }

    @Test
    fun savingReminders_deletingAllReminders() = runBlocking {
        //Given - Saving new reminders in the database
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)
        localRepository.saveReminder(reminder4)

        localRepository.deleteAllReminders()

        //When - Getting all reminders
        val reminders = localRepository.getReminders()

        //Then - The number of retrieved reminders is correct
        reminders as Result.Success
        assertThat(reminders.data.size, `is`(0))

    }

    @Test
    fun gettingReminder_returnsError() = runBlocking {
        localRepository.deleteAllReminders()

        val result = localRepository.getReminder(reminder4.id) as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }


}
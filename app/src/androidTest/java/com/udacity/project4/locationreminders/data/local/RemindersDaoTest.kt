package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt(Done)

    lateinit var db: RemindersDatabase

    private val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
    private val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0, "3")
    private val reminder4 = ReminderDTO("Title4", "Description4", "Location4", 4.0, 4.0, "4")


    // Executing each task synchronously by using the architecture components
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDB() {
        //We used the in-memory db because the information stored here disappearing when the process's killed
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    //Closing the database
    @After
    fun closingDB() = db.close()

    @Test
    fun insertAndGetAllReminders() = runBlockingTest {
        //Given - Insert a reminder
        db.reminderDao().saveReminder(reminder1)
        db.reminderDao().saveReminder(reminder2)
        db.reminderDao().saveReminder(reminder3)
        db.reminderDao().saveReminder(reminder4)

        //When - Getting all the reminders from the database
        val allReminders = db.reminderDao().getReminders()

        //Then - The loaded data has the correct number of reminders
        assertThat(allReminders.size, `is`(4))
    }

    @Test
    fun insertAndGetReminderByID() = runBlockingTest {
        //Given - insert a reminder
        db.reminderDao().saveReminder(reminder3)

        //When - getting the reminder by id from the database
        val reminder = db.reminderDao().getReminderById(reminder3.id)

        //Then - The loaded data contains the expected id for the reminder
        assertThat<ReminderDTO>(reminder as ReminderDTO, notNullValue())
        assertThat(reminder.title, `is`(reminder3.title))
        assertThat(reminder.description, `is`(reminder3.description))
        assertThat(reminder.longitude, `is`(reminder3.longitude))
        assertThat(reminder.latitude, `is`(reminder3.latitude))
        assertThat(reminder.location, `is`(reminder3.location))
        assertThat(reminder.id, `is`(reminder3.id))
    }

    @Test
    fun insertAndDeleteAllReminders() = runBlockingTest {
        //Given - Insert  reminders
        db.reminderDao().saveReminder(reminder1)
        db.reminderDao().saveReminder(reminder2)
        db.reminderDao().saveReminder(reminder3)
        db.reminderDao().saveReminder(reminder4)

        db.reminderDao().deleteAllReminders()

        //When - Get all reminders from the database
        val allReminders = db.reminderDao().getReminders()
        assertThat(allReminders.size, `is`(0))
    }


}
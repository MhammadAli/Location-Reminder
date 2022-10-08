package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

//    TODO: test the navigation of the fragments.(Done)
//    TODO: test the displayed data on the UI.(Done)


    private val dataSource: ReminderDataSource by inject()

    private val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
    private val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0, "3")
    private val reminder4 = ReminderDTO("Title4", "Description4", "Location4", 4.0, 4.0, "4")

    @Before
    fun initRepository() {
        stopKoin()
        // Using Koin library as a service locator
        val myModule = module {
            //Declaring a viewModel to be injected into fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(), get()
                )
            }
            single {
                FakeDataSource() as ReminderDataSource
            }
        }

        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }
    }

    @After
    fun clearDB() = runBlockingTest { dataSource.deleteAllReminders() }

    @Test
    fun remindersList_displayedInUI() = runBlockingTest {
        //Given - Add active (incomplete) reminders to the database
        dataSource.saveReminder(reminder1)
        dataSource.saveReminder(reminder2)
        dataSource.saveReminder(reminder3)
        dataSource.saveReminder(reminder4)

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //Then
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.description)).check(matches(isDisplayed()))
        onView(withText(reminder3.title)).check(matches(isDisplayed()))
        onView(withText(reminder4.location)).check(matches(isDisplayed()))
        onView(withId(R.id.noDataTextView)).check(matches(not(isDisplayed())))
    }

    @Test
    fun remindersList_noReminders() = runBlockingTest {
        //Given - Add active(incomplete) reminders to the database
        dataSource.deleteAllReminders()

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        //Then
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
        onView(withText(reminder2.description)).check(doesNotExist())
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

    @Test
    fun clickingFAB_navigateToReminderFragment() = runBlockingTest {
        //Given - on the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }

        //When - Clicking on add button "+"
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Then - Verify that the app navigates to the save reminder framgent
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}
package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.Is.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects(Done)

    // The variable that will be tested
    lateinit var remindersListViewModel: RemindersListViewModel

    // Using  a fake data source to be injected in the viewModel
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder1 = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0, "1")
    private val reminder2 = ReminderDTO("Title2", "Description2", "Location2", 2.0, 2.0, "2")
    private val reminder3 = ReminderDTO("Title3", "Description3", "Location3", 3.0, 3.0, "3")
    private val reminder4 = ReminderDTO("Title4", "Description4", "Location4", 4.0, 4.0, "4")

    // Executing each task synchronously by using the architecture components
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    // Setting the main coroutine dispatcher for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutinesRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun clearingDataSource() = runBlockingTest { fakeDataSource.deleteAllReminders() }


    @Test
    fun invalidateShowNoData_ShowNoData_isTrue() = mainCoroutinesRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(0))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))

    }

    @Test
    fun loadReminders_loadsFourReminders() = mainCoroutinesRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()
        fakeDataSource.saveReminder(reminder1)
        fakeDataSource.saveReminder(reminder2)
        fakeDataSource.saveReminder(reminder3)
        fakeDataSource.saveReminder(reminder4)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(4))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_checkLoading() = mainCoroutinesRule.runBlockingTest {
        // pausing the dispatcher then we can  verify the initial values
        mainCoroutinesRule.pauseDispatcher()
        fakeDataSource.deleteAllReminders()
        fakeDataSource.saveReminder(reminder2)

        //When loading the reminders
        remindersListViewModel.loadReminders()

        //Then loading indicator is shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Executing pending coroutines actions
        mainCoroutinesRule.resumeDispatcher()

        //Then loading indicator is not visible
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun loadReminders_shouldReturn_remindersNotFound() = mainCoroutinesRule.runBlockingTest {
        //When
        fakeDataSource.adjustReturnErrorOrNot(true)
        remindersListViewModel.loadReminders()

        //Then
        assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Reminders not found")
        )
    }


}
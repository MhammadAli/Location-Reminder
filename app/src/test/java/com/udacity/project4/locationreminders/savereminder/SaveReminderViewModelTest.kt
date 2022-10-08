package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.data.dto.Result

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects(Done)


    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Using  a fake data source to be injected in the viewModel
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder1Complete =
        ReminderDataItem("Title1", "Description1", "Location1", 1.0, 1.0, "1")
    private val reminder2WithNoTitle =
        ReminderDataItem("", "Description2", "Location2", 2.0, 2.0, "2")
    private val reminder3WithNoLocation =
        ReminderDataItem("Title3", "Description3", "", 3.0, 3.0, "3")

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
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun onClear_clearingRemindersLiveData() {
        //Given
        saveReminderViewModel.reminderTitle.value = reminder1Complete.title
        saveReminderViewModel.reminderDescription.value = reminder1Complete.description
        saveReminderViewModel.reminderSelectedLocationStr.value = reminder1Complete.location
        saveReminderViewModel.latitude.value = reminder1Complete.latitude
        saveReminderViewModel.longitude.value = reminder1Complete.longitude

        //When
        saveReminderViewModel.onClear()

        //Then
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            `is`(nullValue())
        )
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))


    }

    @Test
    fun saveReminder_addingReminderToDataSource() = mainCoroutinesRule.runBlockingTest {
        //When
        saveReminderViewModel.saveReminder(reminder1Complete)
        val checkingReminder = fakeDataSource.getReminder("1") as Result.Success

        //Then
        assertThat(checkingReminder.data.title, `is`(reminder1Complete.title))
        assertThat(checkingReminder.data.description, `is`(reminder1Complete.description))
        assertThat(checkingReminder.data.location, `is`(reminder1Complete.location))
        assertThat(checkingReminder.data.latitude, `is`(reminder1Complete.latitude))
        assertThat(checkingReminder.data.longitude, `is`(reminder1Complete.longitude))
        assertThat(checkingReminder.data.id, `is`(reminder1Complete.id))
    }

    @Test
    fun savingReminder_checkLoading() = mainCoroutinesRule.runBlockingTest {
        // pausing the dispatcher then we can  verify the initial values
        mainCoroutinesRule.pauseDispatcher()

        //When
        saveReminderViewModel.saveReminder(reminder1Complete)

        //Then loading indicator is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Executing pending coroutines actions
        mainCoroutinesRule.resumeDispatcher()

        //Then loading indicator is not visible
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validatingData_missingLocation_showingSnackBarThenReturnFalse() {
        //When
        val validateLocation = saveReminderViewModel.validateEnteredData(reminder3WithNoLocation)

        //Then
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
        assertThat(validateLocation, `is`(false))
    }

    @Test
    fun validatingData_missingTitle_showingSnackBarThenReturnFalse() {
        //When

        val validateTitle = saveReminderViewModel.validateEnteredData(reminder2WithNoTitle)

        //Then
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
        assertThat(validateTitle, `is`(false))
    }


}
package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // Use a fake repository to be injected into the viewmodel
    private lateinit var remindersRepository: FakeDataSource

    private lateinit var reminderViewModel: RemindersListViewModel

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        Dispatchers.setMain(testDispatcher)
        // We initialise the tasks to 3, with one active and two completed
        remindersRepository = FakeDataSource()

        reminderViewModel =
            RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                remindersRepository
            )

    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        stopKoin()
    }

    @Test
    fun loadReminders_loading()   {
        mainCoroutineRule.pauseDispatcher()
        reminderViewModel.loadReminders()
        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))
        mainCoroutineRule.resumeDispatcher()
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }


    @Test
    fun addReminders_getReminders() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343
        )
        val reminder2 = ReminderDTO(
            "title2", "description2", "location2", 0.343, 0.4343
        )
        val reminder3 = ReminderDTO(
            "title3", "description3", "location3", 0.343, 0.4343
        )
        remindersRepository.saveReminder(reminder)
        remindersRepository.saveReminder(reminder2)
        remindersRepository.saveReminder(reminder3)
        reminderViewModel.loadReminders()
        val loaded: List<ReminderDataItem> = reminderViewModel.remindersList.getOrAwaitValue()
        assertThat(loaded.size, `is`(3))
        assertThat(loaded[0].id, `is`(reminder.id))
        assertThat(loaded[1].id, `is`(reminder2.id))
        assertThat(loaded[2].id, `is`(reminder3.id))
    }

    @Test
    fun loadReminders_callError() = mainCoroutineRule.runBlockingTest {
        remindersRepository.setReturnError(true)
        reminderViewModel.loadReminders()
        assertThat(reminderViewModel.showSnackBar.getOrAwaitValue(), `is`("Test exception"))
        assertThat(reminderViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }


}
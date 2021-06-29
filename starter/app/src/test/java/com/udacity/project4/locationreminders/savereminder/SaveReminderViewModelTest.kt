package com.udacity.project4.locationreminders.savereminder

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var remindersRepository: FakeDataSource

    private lateinit var saveReminderViewModel: SaveReminderViewModel

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    private lateinit var reminderDataItem: ReminderDataItem

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        Dispatchers.setMain(testDispatcher)
        // We initialise the tasks to 3, with one active and two completed
        remindersRepository = FakeDataSource()

        reminderDataItem = ReminderDataItem(
            "title",
            "description",
            "location",
            1.23,
            3.23
        )

        saveReminderViewModel =
            SaveReminderViewModel(
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
    fun validateEnteredData_nullTitle() = mainCoroutineRule.runBlockingTest {
        reminderDataItem.title = null
        val loaded: Boolean = saveReminderViewModel.validateEnteredData(reminderDataItem)
        val snackBar = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        MatcherAssert.assertThat(
            snackBar,
            `is`(R.string.err_enter_title)
        )
        MatcherAssert.assertThat(
            loaded,
            `is`(false)
        )
    }

    @Test
    fun validateEnteredData_emptyLocation() = mainCoroutineRule.runBlockingTest {
        reminderDataItem.location = ""
        val loaded: Boolean = saveReminderViewModel.validateEnteredData(reminderDataItem)
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
        MatcherAssert.assertThat(
            loaded,
            `is`(false)
        )
    }

    @Test
    fun saveReminder_setNewReminder() = mainCoroutineRule.runBlockingTest {
        val loaded = saveReminderViewModel.validateAndSaveReminder(reminderDataItem)
        MatcherAssert.assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`("Reminder Saved !")
        )
        MatcherAssert.assertThat(
            loaded,
            `is`(true)
        )
    }

    @Test
    fun saveReminder_saved() = mainCoroutineRule.runBlockingTest {
        val loaded = saveReminderViewModel.validateAndSaveReminder(reminderDataItem)
        MatcherAssert.assertThat(
            loaded,
            `is`(true)
        )
    }

}
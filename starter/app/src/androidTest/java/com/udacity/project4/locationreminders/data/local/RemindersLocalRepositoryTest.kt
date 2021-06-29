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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
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

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()
    private lateinit var localDataSource: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
// Replace with runBlockingTest once issue is resolved
    @Test
    fun saveReminder_retrieves_returnReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminderNew = ReminderDTO(
            "title", "description", "location", 0.343, 0.4343
        )
        localDataSource.saveReminder(reminderNew)

        // WHEN  - Reminder retrieved by ID.
        val result = localDataSource.getReminder(reminderNew.id)

        // THEN - Same reminder is returned.
        result as Result.Success
        assertThat(result.data, `is`(reminderNew))
    }

    @Test
    fun saveReminders_retrieve_returnReminders() = runBlocking {
        // GIVEN
        val reminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343
        )
        val reminder2 = ReminderDTO(
            "title2", "description2", "location2", 0.343, 0.4343
        )
        val reminder3 = ReminderDTO(
            "title3", "description3", "location3", 0.343, 0.4343
        )
        localDataSource.saveReminder(reminder)
        localDataSource.saveReminder(reminder2)
        localDataSource.saveReminder(reminder3)
        // WHEN
        val result = localDataSource.getReminders()

        // THEN
        result as Result.Success
        assertThat(result.data[0], `is`(reminder))
        assertThat(result.data[1], `is`(reminder2))
        assertThat(result.data[2], `is`(reminder3))
    }


    @Test
    fun saveReminders_delete_returnZeroSize() = runBlocking {
        // GIVEN
        val reminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343
        )
        val reminder2 = ReminderDTO(
            "title2", "description2", "location2", 0.343, 0.4343
        )
        val reminder3 = ReminderDTO(
            "title3", "description3", "location3", 0.343, 0.4343
        )
        localDataSource.saveReminder(reminder)
        localDataSource.saveReminder(reminder2)
        localDataSource.saveReminder(reminder3)
        // WHEN
        localDataSource.deleteAllReminders()

        // THEN
        val result = localDataSource.getReminders()
        result as Result.Success
        assertThat(result.data.size, `is`(0))
    }

    @Test
    fun getReminder_error_returnErrorNoReminder() = runBlocking {
        // GIVEN
        val reminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343
        )

        // WHEN
        val result = localDataSource.getReminder(reminder.id)

        // THEN
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }


}
package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343
        )
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        // 1. Insert a reminder into the DAO.
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343
        )
        database.reminderDao().saveReminder(reminder)
        val id = reminder.id


        // 2. Update the reminder by creating a new reminder with the same ID but different attributes.
        val newReminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343, id
        )
        database.reminderDao().saveReminder(newReminder)

        // 3. Check that when you get the reminder by its ID, it has the updated values.
        val loaded = database.reminderDao().getReminderById(id)
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(newReminder.id))
        assertThat(loaded.title, `is`(newReminder.title))
        assertThat(loaded.description, `is`(newReminder.description))
        assertThat(loaded.location, `is`(newReminder.location))
        assertThat(loaded.latitude, `is`(newReminder.latitude))
        assertThat(loaded.longitude, `is`(newReminder.longitude))
    }

    @Test
    fun insertRemindersAndGetAll() = runBlockingTest {
        // GIVEN - Insert reminders.
        val reminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343
        )
        val reminder2 = ReminderDTO(
            "title2", "description2", "location2", 0.343, 0.4343
        )
        val reminder3 = ReminderDTO(
            "title3", "description3", "location3", 0.343, 0.4343
        )
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)


        // WHEN - Get the reminders from the database.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.size, `is`(3))
        assertThat(loaded[0], `is`(reminder))
        assertThat(loaded[1], `is`(reminder2))
        assertThat(loaded[2], `is`(reminder3))
    }

    @Test
    fun deleteReminders() = runBlockingTest {
        // GIVEN - Insert reminders.
        val reminder = ReminderDTO(
            "title", "description", "location", 1.343, 3.4343
        )
        val reminder2 = ReminderDTO(
            "title2", "description2", "location2", 0.343, 0.4343
        )
        val reminder3 = ReminderDTO(
            "title3", "description3", "location3", 0.343, 0.4343
        )
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)


        // WHEN - Delete all reminders from the database.
        database.reminderDao().deleteAllReminders()

        // THEN - The loaded data contains the expected values.
        val loaded = database.reminderDao().getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.size, `is`(0))
    }

}
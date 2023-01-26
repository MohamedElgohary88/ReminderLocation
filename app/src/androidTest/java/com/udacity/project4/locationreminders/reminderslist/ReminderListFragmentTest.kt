package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.koin.test.get
import org.koin.test.AutoCloseKoinTest

@ExperimentalCoroutinesApi
@MediumTest
@RunWith(AndroidJUnit4::class)
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private val mockNavController = mock(NavController::class.java)
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var reminderDataSource: ReminderDataSource

    @Before
    fun initializationModules() {
        stopKoin()
        val module = module {
            single {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            viewModel {
                RemindersListViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }
            single {
                LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext())
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }

        }
        startKoin {
            modules(listOf(module))
        }
        reminderDataSource = get()
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), reminderDataSource)
    }

    //    TODO: test the displayed data on the UI.
    @Test
    fun dataDisplayed() {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText("No Data")).check(matches(isDisplayed()))
    }

    //    TODO: add testing for the error messages.
    @Test
    fun navigateOnclick() {
        val sec = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        sec.onFragment { Navigation.setViewNavController(it.view!!, mockNavController) }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(mockNavController).navigate(ReminderListFragmentDirections.toSaveReminder())

    }

    @Test
    fun reminderDisplayed() {
        val remind = ReminderDTO("1", "2", "3", 111.1, 120.9)
        runBlocking {
            reminderDataSource.saveReminder(remind)
        }
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withText(remind.title)).check(matches(isDisplayed()))
        onView(withText(remind.description)).check(matches(isDisplayed()))
        onView(withText(remind.location)).check(matches(isDisplayed()))
    }
}
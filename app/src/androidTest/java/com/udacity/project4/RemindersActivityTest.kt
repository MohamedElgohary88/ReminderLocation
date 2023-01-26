package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.hamcrest.core.IsNot
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@LargeTest
@RunWith(AndroidJUnit4::class)
class RemindersActivityTest :
    AutoCloseKoinTest() {
    private val dataBindingIdlingResource = DataBindingIdlingResource()
    private lateinit var application: Application
    private lateinit var reminderDataSource: ReminderDataSource

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun initializationSingle() {
        stopKoin()//stop the original app koin
        application = getApplicationContext()
        val myModule = module {
            viewModel { RemindersListViewModel(application, get() as ReminderDataSource ) }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(application) }
            single { SaveReminderViewModel( application, get() as ReminderDataSource ) }

        }
        startKoin { modules(listOf(myModule)) }
        reminderDataSource = get()
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }

    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

    @Before
    fun registerDataBindingIdling() {
        IdlingRegistry.getInstance()
            .register(com.udacity.project4.utils.Espresso.counting_id_resource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun afterRegistered() {
        IdlingRegistry.getInstance()
            .register(com.udacity.project4.utils.Espresso.counting_id_resource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @Test
    fun onClickFloatingActionButton() = runBlocking {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderDescription)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun showReminderToast() = runBlocking{
        val remindersActivityActivityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(remindersActivityActivityScenario)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("Please enter title")).inRoot(withDecorView(
            IsNot.not(Matchers.`is`(getActivity(remindersActivityActivityScenario)?.window?.decorView)))).check(matches(isDisplayed()))
        remindersActivityActivityScenario.close()
    }

    @Test
    fun faulty_Reminder() = runBlocking {
        val activityActivityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityActivityScenario)
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("Please enter title")).check(matches(isDisplayed()))
        activityActivityScenario.close()
    }

}



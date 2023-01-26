package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private val reminderDataItem1 = ReminderDataItem("1", "2", "3", 100.0, 100.0)
    private val reminderDataItem = ReminderDataItem("1", "2", "3", 123.0, 124.0)
    private lateinit var reminderViewModel: SaveReminderViewModel
    private val longitude = LatLng(120.0, 150.0)
    private val poi = PointOfInterest(longitude, "placeOne", "A special player")

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private val fault = ReminderDataItem("", "", "", 123.0, 125.0)

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setViewModel() {
        val app: MyApp = ApplicationProvider.getApplicationContext()
        fakeDataSource = FakeDataSource()
        reminderViewModel = SaveReminderViewModel(app, fakeDataSource)
    }

    @Before
    fun tearViewModel() {
        stopKoin()
    }

    @Test
    fun confirmLocation() {
        reminderViewModel.confirmLoc(longitude, poi)
        assertThat(
            reminderViewModel.navigationCommand.value,
            IsInstanceOf(NavigationCommand.Back::class.java)
        )
        assertThat(reminderViewModel.selectedPOI.value, `is`(poi))
        assertThat(reminderViewModel.reminderSelectedLocationStr.value, `is`(poi.name))
        assertThat(reminderViewModel.longitude.value, `is`(longitude.longitude))
        assertThat(reminderViewModel.latitude.value, `is`(longitude.latitude))
        reminderViewModel.onClear()
    }

    @Test
    fun validateAndSaveReminders() = runBlockingTest {
        reminderViewModel.validateAndSaveReminder(reminderDataItem)
        reminderViewModel.validateAndSaveReminder(fault)
        fakeDataSource.deleteAllReminders()
        val error1 = fakeDataSource.getReminder(fault.id) as Result.Error
        assertThat(fakeDataSource.getReminder(reminderDataItem.id), `is`(notNullValue()))
        assertThat(error1.statusCode, `is`(nullValue()))
    }

    @Test
    fun validateReminder() = runBlockingTest {
        assertThat(reminderViewModel.validateEnteredData(fault), `is`(false))
        assertThat(reminderViewModel.validateEnteredData(reminderDataItem), `is`(true))
    }

    //testing of toast
    @Test
    fun saveReminder_ShowToast() = runBlockingTest {
        reminderViewModel.saveReminder(reminderDataItem1)
        val value = reminderViewModel.showToast.getOrAwaitValue()
        assertThat(value, `is`(ApplicationProvider.getApplicationContext<Context>().getString(R.string.reminder_saved)))
    }
}
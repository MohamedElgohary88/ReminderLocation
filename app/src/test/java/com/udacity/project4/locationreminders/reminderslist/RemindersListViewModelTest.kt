package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import junit.framework.TestCase.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(maxSdk = Build.VERSION_CODES.P)

class RemindersListViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setup_VIEWMODEL() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun shutDown_VIEWMODEL() {
        stopKoin()
    }

    @Test
    fun shouldReturnEmpty() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()
        val res = remindersListViewModel.showSnackBar.value
        assertThat(res, `is`(nullValue()))
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun checkError() {
        remindersListViewModel.loadReminders()
        fakeDataSource.setReturnError(true)
        assertEquals("error", remindersListViewModel.showSnackBar.getOrAwaitValue())
    }

    @Test
    fun remindersEmpty() = runBlockingTest {
        remindersListViewModel.loadReminders()
        fakeDataSource.deleteAllReminders()
        assertThat(remindersListViewModel.showNoData.value, `is`(true))
    }

    @Test
    fun checkLoading() = mainCoroutineRule.runBlockingTest {
        assertTrue(remindersListViewModel.showLoading.getOrAwaitValue())
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        mainCoroutineRule.resumeDispatcher()
        assertFalse(remindersListViewModel.showLoading.getOrAwaitValue())
    }
}
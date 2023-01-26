package com.udacity.project4.locationreminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigation
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class ReminderViewModel(val app: Application, val datasource: ReminderDataSource) :
    AndroidViewModel(app) {
    val reminderHasUpdated = MutableLiveData<Boolean>(false)
}

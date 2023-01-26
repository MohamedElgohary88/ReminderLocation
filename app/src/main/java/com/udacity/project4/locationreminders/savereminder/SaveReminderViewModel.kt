package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val selectedPOI = MutableLiveData<PointOfInterest>()
    val locIsCon=MutableLiveData<Boolean>(false)
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem):Boolean {
    return if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
             true
        }
        else  false
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            showSnackBar.value="Geofence added"
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showToast.value=app.getString(R.string.err_enter_title)
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showToast.value=app.getString(R.string.err_select_location)
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
    fun confirmLoc(latlng : LatLng,Poi:PointOfInterest){
        //Confirming all the new location data
        locIsCon.value=false
        latitude.value=latlng.latitude
        reminderSelectedLocationStr.value=Poi.name

        longitude.value=latlng.longitude
        selectedPOI.value=Poi
        navigationCommand.postValue(NavigationCommand.Back)

    }
    fun setLocationAsConfiremed(data: Boolean){
locIsCon.value= data
    }
    fun savePoi(poi:PointOfInterest?){
        latitude.value=poi?.latLng?.latitude
        reminderSelectedLocationStr.value=poi?.name
        longitude.value=poi?.latLng?.longitude
        selectedPOI.value=poi
    }
}
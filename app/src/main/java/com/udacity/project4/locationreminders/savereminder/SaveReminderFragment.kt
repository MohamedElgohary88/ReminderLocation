package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
//import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    //VARIABLES
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 29

    private var PERMISSION_REQUEST_CODE = 0
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "HuntMainActivity.treasureHunt.action.ACTION_GEOFENCE_EVENT"
    }

    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val TAG = "SaveReminderFragment"

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private var activity = Activity()

    private lateinit var geofenceClient: GeofencingClient
    private lateinit var reminderDataItem: ReminderDataItem

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(activity, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        geofenceClient = LocationServices.getGeofencingClient(activity)

        binding.saveReminder.setOnClickListener { reminder_toDataBase() }
        binding.selectLocation.setOnClickListener { goToSelectLocation() }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun reminder_toDataBase() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        reminderDataItem = ReminderDataItem(title, description, location, latitude, longitude)
        permissionAndStartGeofencing()
    }


    private fun goToSelectLocation() {
        _viewModel.navigationCommand.value =
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
    }

    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.activity = activity
    }

    private fun permissionAndStartGeofencing() {
        if (!foreAndBackLocationPermissions())
            reqest_foregroundAndBackgroundLocationPermission()
        else {
            checkDevice_LocationAndStart_Geofence()
        }
    }

    private fun checkDevice_LocationAndStart_Geofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingResponse = settingClient.checkLocationSettings(builder.build())
        locationSettingResponse.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog
                try {
                    startIntentSenderForResult(
                        exception.resolution.intentSender,
                        REQUEST_TURN_DEVICE_LOCATION_ON,
                        null, 0, 0, 0, null
                    )

                } catch (sendException: IntentSender.SendIntentException) {
                    Log.d(
                        TAG,
                        "errorSendingLocation: ${sendException.message} "
                    )
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        checkDevice_LocationAndStart_Geofence()
                    }.show()
            }
        }
        locationSettingResponse.addOnCompleteListener {
            if (it.isSuccessful) {
                geofenceForReminder()
            }
        }
    }

    private fun geofenceForReminder() {
        if (!this::reminderDataItem.isInitialized) return
        if (_viewModel.validateAndSaveReminder(reminderDataItem)) {
            val currentReminderDataItem = reminderDataItem
            val geofence = geofence(currentReminderDataItem)
            val geofenceRequest = geoFenceReq(geofence)
            handleGeofenceClient(geofencePendingIntent, geofenceRequest, geofence)
            _viewModel.onClear()
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleGeofenceClient(
        geofencePendingIntent: PendingIntent?,
        geofenceRequest: GeofencingRequest?,
        geofence: Geofence?
    ) {
        geofenceClient.addGeofences(geofenceRequest, geofencePendingIntent).run {
            addOnSuccessListener {
                Log.d(TAG, "addGeofenceForReminder: ${geofence?.requestId}")

            }
            addOnFailureListener {
                if (it.message != null) {
                    Log.d(TAG, "Failed To add geofence: ${it.message}")
                }
            }
        }
    }

    private fun geoFenceReq(geofence: Geofence?): GeofencingRequest? {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    private fun geofence(currentGeofenceData: ReminderDataItem): Geofence? {
        return Geofence.Builder()
            .setRequestId(currentGeofenceData.id).setCircularRegion(
                currentGeofenceData.latitude!!,
                currentGeofenceData.longitude!!,
                2000f
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()
    }

    @TargetApi(29)
    private fun foreAndBackLocationPermissions(): Boolean {
        val foregroundLocationApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        val backgroundPermissionApproved = if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true
        }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(android.os.Build.VERSION_CODES.Q)
    private fun reqest_foregroundAndBackgroundLocationPermission() {
        if (!foreAndBackLocationPermissions()) {
            var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            PERMISSION_REQUEST_CODE = when {
                runningQOrLater -> {
                    permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                }
                else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE

            }
            Log.d(TAG, getString(R.string.error_happened))
            requestPermissions(
                permissionArray,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            checkDevice_LocationAndStart_Geofence()
        } else {
            Snackbar.make(
                binding.root,
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                reqest_foregroundAndBackgroundLocationPermission()
            }.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDevice_LocationAndStart_Geofence(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }
}

package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import android.location.Geocoder
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.udacity.project4.BuildConfig.APPLICATION_ID
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

@Suppress("NAME_SHADOWING")
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder

    private lateinit var map: GoogleMap
    override val _viewModel: SaveReminderViewModel by inject()
    private var marker1: Marker? = null
    private var pointOfInterest: PointOfInterest? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: FragmentSelectLocationBinding
    private val REQUEST_CODE_LOCATION_PERMISSION = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireContext())

        val mapFrag = childFragmentManager.findFragmentById(R.id.map1) as SupportMapFragment
        mapFrag.getMapAsync(this)
        binding.savelocation.setOnClickListener { selectedLocation() }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        _viewModel.locIsCon.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it) {
                selectedLocation()
            }
        })
    }

    //PERMISSION METHODS
    private fun isPermissiongranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun foregroundPermissionEnabled(): Boolean {
        return (PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ))
    }

    private fun requestForegroundPermission() {
        val permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        requestPermissions(permissionArray, REQUEST_CODE_LOCATION_PERMISSION)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (!foregroundPermissionEnabled()) {
            when {
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    Snackbar.make(requireView(), getString(R.string.per1), Snackbar.LENGTH_SHORT)
                        .setAction(getString(R.string.enable1)) {
                            requestForegroundPermission()
                        }.show()
                }
                else -> {
                    Snackbar.make(requireView(), "location denied", Snackbar.LENGTH_SHORT)
                        .setAction("change Permission") {
                            startActivity(Intent().apply {
                                data =
                                    Uri.fromParts("package", APPLICATION_ID, null)

                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS

                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            })
                        }.show()
                }
            }
        } else {
            accessLocation()
        }
    }

    private fun selectedLocation() {
        if (pointOfInterest != null) {
            _viewModel.savePoi(pointOfInterest)
            findNavController().navigate(SelectLocationFragmentDirections.actionSelectLocationFragmentToSaveReminderFragment())
        } else {
            Toast.makeText(context, "Please select a location", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun accessLocation() {
        if (isPermissiongranted()) {
            map.isMyLocationEnabled = true
            zoomTocurrentLocation(true)
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                _viewModel.showSnackBar.postValue(getString(R.string.permission_denied_explanation))
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun zoomTocurrentLocation(f: Boolean) {
        val requestLocation =
            LocationRequest.create().apply { priority = LocationRequest.PRIORITY_LOW_POWER }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(requestLocation)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener {
            if (it !is ResolvableApiException || !f) {
                Snackbar.make(
                    binding.root,
                    "Must Enable Location services",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Ok") { zoomTocurrentLocation(true) }.show()
            } else {
                startIntentSenderForResult(
                    it.resolution.intentSender,
                    1002, null,
                    0, 0, 0, null
                )
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        val zoom = 15f
                        val home = LatLng(loc.latitude, loc.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom))
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setPointOnClickListener(map)
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.map_style))
        setMapListener(map)
        accessLocation()
    }

    private fun setMapListener(map: GoogleMap) {
        map.setOnMapClickListener {
            val address =
                Geocoder(context, Locale.getDefault()).getFromLocation(it.latitude, it.longitude, 1)
            when {
                address.isNotEmpty() -> {
                    val address: String = address[0].getAddressLine(0)
                    val addressPOI = PointOfInterest(it, null, address)
                    marker1?.remove()
                    val marker = map.addMarker(
                        MarkerOptions().position(addressPOI.latLng).title(addressPOI.name)
                    )
                    marker.showInfoWindow()
                    marker1 = marker
                    pointOfInterest = addressPOI
                }
            }
        }
    }

    private fun setPointOnClickListener(map: GoogleMap) {
        map.setOnPoiClickListener {
            marker1?.remove()
            val marker = map.addMarker(MarkerOptions().position(it.latLng).title(it.name))
            marker.showInfoWindow()
            pointOfInterest = it
            marker1 = marker

        }
    }

    //MENU METHODS
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {

            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {

            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}


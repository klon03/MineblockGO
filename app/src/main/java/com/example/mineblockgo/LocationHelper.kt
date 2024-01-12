package com.example.mineblockgo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHelper(private val context: Context, private val onLocationChange: (Location) -> Unit, private val overlayView: FrameLayout) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val permissionRequestLocation = 1
    init {
        initializeLocationClient()
        createLocationCallback()

        val buttonInOverlay: Button = overlayView.findViewById(R.id.requestButton)
        buttonInOverlay.setOnClickListener {
            openAppSettings()
            hidePermissionOverlay()
        }
    }

    private fun initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                onLocationChange(locationResult.lastLocation)
            }
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Interwał w milisekundach
            fastestInterval = 5000 // Najkrótszy interwał w milisekundach
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // Poproś użytkownika o uprawnienia lokalizacyjne
            requestLocationPermission()
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }



    fun startLocationUpdates() {
        requestLocationUpdates()
    }

    /** Uprawnienia **/
    private fun requestLocationPermission() {
        overlayView.visibility = View.VISIBLE
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            permissionRequestLocation
        )
    }

    private fun openAppSettings() {
        hidePermissionOverlay()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun hidePermissionOverlay() {
        overlayView.visibility = View.GONE
    }
}

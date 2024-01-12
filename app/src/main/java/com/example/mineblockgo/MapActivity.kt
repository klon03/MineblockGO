package com.example.mineblockgo

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mineblockgo.databinding.ActivityMapBinding
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private lateinit var permissionOverlay: FrameLayout
    private lateinit var locationHelper: LocationHelper
    private var currentLocationMarker: Marker? = null
    private lateinit var centerUserBtn: ImageButton


    private val permissionRequestLocation = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Przypisywanie widoków
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionOverlay = findViewById(R.id.overlayPermission)
        centerUserBtn = findViewById(R.id.centerUserBtn)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationHelper = LocationHelper(this, { location -> updateLocationMarker(location) }, permissionOverlay)
    }
    override fun onResume() {
        super.onResume()
        locationHelper.startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        locationHelper.stopLocationUpdates()
    }

    // Obsługa wyniku żądania uprawnień
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionRequestLocation -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationHelper.hidePermissionOverlay()
                } else {
                }
            }
        }
    }

    private fun updateLocationMarker(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        if (currentLocationMarker == null) {
            // Jeżeli marker jeszcze nie istnieje, stwórz go
            val customMarkerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.steve)
            val scaledBitmap = Bitmap.createScaledBitmap(customMarkerBitmap, 96, 216, false)
            val customMarkerIcon: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

            currentLocationMarker = mMap.addMarker(MarkerOptions().position(currentLatLng).title("Ty").icon(customMarkerIcon))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))

        } else {
            // Jeżeli marker już istnieje, zaktualizuj jego pozycję
            currentLocationMarker?.position = currentLatLng
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        centerUserBtn.setOnClickListener {
            currentLocationMarker?.let { marker ->
                marker.position?.let { position ->
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
                }
            }
        }
    }
}
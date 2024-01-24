package com.example.mineblockgo

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.gms.maps.model.Circle



class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val MONSTER_REQUEST = 123
    }
    enum class MainButtonMode {
        DEFAULT,
        COMBAT,
        CHEST,
        SHOP
    }
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private lateinit var permissionOverlay: FrameLayout
    private lateinit var locationHelper: LocationHelper
    private var currentLocationMarker: Marker? = null
    private lateinit var centerUserBtn: ImageButton
    private lateinit var locationManager: LocationManager
    private lateinit var mainBtn: ImageButton
    private var mainBtnState: MainButtonMode? = null
    private lateinit var circle: Circle
    private lateinit var settingsBtn: ImageButton
    private lateinit var inventoryBtn: ImageButton


    private val permissionRequestLocation = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Przypisywanie widoków
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionOverlay = findViewById(R.id.overlayPermission)

        centerUserBtn = findViewById(R.id.centerUserBtn)
        mainBtn = findViewById(R.id.mainBtn)
        settingsBtn = findViewById(R.id.settingsBtn)
        inventoryBtn = findViewById(R.id.inventoryBtn)
        updateMainBtn(MainButtonMode.DEFAULT)

        inventoryBtn.setOnClickListener {
            // TODO
        }

        settingsBtn.setOnClickListener {
            // TODO
        }

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
                }
            }
        }
    }

    private fun updateLocationMarker(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        if (currentLocationMarker == null) {
            // Jeżeli marker jeszcze nie istnieje, tworzy marker gracza
            val customMarkerBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.steve)
            val scaledBitmap = Bitmap.createScaledBitmap(customMarkerBitmap, 96, 216, false)
            val customMarkerIcon: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

            currentLocationMarker = mMap.addMarker(MarkerOptions().position(currentLatLng).title("You").icon(customMarkerIcon))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))

            locationManager = LocationManager(this,mMap, currentLocationMarker!!)
            locationManager.loadEntitiesOnStartup()
            updateMainBtn(locationManager.checkVicinity())


            /** // Testowe koło wokół gracza
            val circleOptions = CircleOptions()
                .center(currentLocationMarker!!.position)
                .radius(1500.0)
                .strokeWidth(2f) // Grubość obramowania koła
                .strokeColor(Color.RED) // Kolor obramowania koła
                .fillColor(Color.argb(70, 255, 0, 0)) // Kolor wypełnienia koła (70% przezroczystości)


             circle = mMap.addCircle(circleOptions)
            **/
        } else {
            // Jeżeli marker już istnieje, zaktualizuj jego pozycję
            if (currentLocationMarker?.position != currentLatLng) {
                currentLocationMarker?.position = currentLatLng
                updateMainBtn(locationManager.checkVicinity())
                locationManager.checkEntitiesOnMove()

//                circle.center= currentLatLng
            }
        }
    }

    private fun turnOffBtn(btn: ImageButton) {
        btn.isEnabled = false
        btn.alpha = 0.9f
        btn.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0.25f) })
        btn.background.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0.25f) })
    }
    private fun turnOnBtn(btn: ImageButton) {
        btn.isEnabled = true
        btn.alpha = 1f
        btn.colorFilter = null
        btn.background.colorFilter = null
    }

    private fun updateMainBtn(newState: MainButtonMode) {
        if (newState != mainBtnState) {
            mainBtnState = newState
            when(mainBtnState) {
                MainButtonMode.DEFAULT -> {
                    turnOffBtn(mainBtn)
                    mainBtn.setBackgroundResource(R.drawable.fight_btn_bg)
                    mainBtn.setImageResource(R.drawable.diamond_sword)
                }

                MainButtonMode.COMBAT -> {
                    turnOnBtn(mainBtn)
                    mainBtn.setBackgroundResource(R.drawable.fight_btn_bg)
                    mainBtn.setImageResource(R.drawable.diamond_sword)
                }
                MainButtonMode.CHEST -> {
                    turnOnBtn(mainBtn)
                    mainBtn.setBackgroundResource(R.drawable.chest_btn_bg)
                    mainBtn.setImageResource(R.drawable.chest1)
                }
                MainButtonMode.SHOP -> {
                    turnOnBtn(mainBtn)
                    mainBtn.setBackgroundResource(R.drawable.shop_btn_bg)
                    //mainBtn.setImageResource(R.drawable.chest1)
                }
                else -> {
                    turnOffBtn(mainBtn)
                }
            }
        }

    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        map.uiSettings.isMapToolbarEnabled = false

        centerUserBtn.setOnClickListener {
            currentLocationMarker?.let { marker ->
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 16f))
            }
        }

        mainBtn.setOnClickListener() {
            locationManager.startActivity()
        }
    }
}




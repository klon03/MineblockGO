package com.example.mineblockgo


import android.annotation.SuppressLint
import android.app.Activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton

import androidx.appcompat.app.AppCompatActivity
import com.example.mineblockgo.databinding.ActivityMapBinding

import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions



class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    enum class MainButtonMode {
        DEFAULT,
        COMBAT,
        CHEST,
        SHOP
    }
    private val databaseHelper = DatabaseManager.getDatabaseInstance()
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
    private lateinit var lvlTxt: TextView
    private lateinit var expTxt: TextView
    private var oldLevel: Int = 0

    private val combatActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            // Obsługa wyniku z CombatActivity
            val data: Intent? = result.data
            val tag = data?.getStringExtra("tag")
            if (tag != null) {
                locationManager.deleteEntity(tag, "monsters")
                updateMainBtn(locationManager.checkVicinity())
            }
        }
    }

    private val chestActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Obsługa wyniku z CombatActivity
            val data: Intent? = result.data
            val tag = data?.getStringExtra("tag")
            if (tag != null) {
                locationManager.deleteEntity(tag, "chests")
                updateMainBtn(locationManager.checkVicinity())
            }
        }
    }


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
        turnOffBtn(mainBtn)
        lvlTxt = findViewById(R.id.userLevelTxt)
        expTxt = findViewById(R.id.userExpTxt)
        updateExp()
        inventoryBtn.setOnClickListener {
            startActivity(Intent(this, EQActivity::class.java))
        }

        settingsBtn.setOnClickListener {
            val settingsIntent = Intent(this, Settings::class.java)
            startActivity(settingsIntent)
        }



        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationHelper = LocationHelper(this, { location -> updateLocationMarker(location) }, permissionOverlay)



    }
    override fun onResume() {
        super.onResume()
        locationHelper.startLocationUpdates()
        updateExp()
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

            locationManager = LocationManager(this, mMap, currentLocationMarker!!)
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
        btn.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1f) })
        btn.background.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1f) })
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
                    mainBtn.setImageResource(R.drawable.gold_ingot)
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

//        if (AppCompatDelegate.MODE_NIGHT_YES) {
//            mMap.setMapStyle(
//                MapStyleOptions()
//            )
//        } TODO

        centerUserBtn.setOnClickListener {
            currentLocationMarker?.let { marker ->
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 16f))
            }
        }

        mainBtn.setOnClickListener() {
            startActivity()
        }
    }

    // Uruchomienie odpowiedniej aktywności po kliknięciu głównego przycisku
    private fun startActivity() {
        if (locationManager.entityInRange != null) {
            when(locationManager.entityInRangeType) {
                MainButtonMode.COMBAT -> {

                    val intent = Intent(this, CombatActivity::class.java)
                    intent.putExtra("tag", locationManager.entityInRange)
                    combatActivityResultLauncher.launch(intent)

                }
                MainButtonMode.CHEST -> {

                    val intent = Intent(this, ChestActivity::class.java)
                    intent.putExtra("tag", locationManager.entityInRange)
                    chestActivityResultLauncher.launch(intent)
                }
                MainButtonMode.SHOP -> {
                    val intent = Intent(this, ShopActivity::class.java)
                    startActivity(intent)
                }
                else -> {}
            }
        }
    }

    private fun calculateLvl(exp: Int): Int {
        var leftExp = exp
        var expReq = 20
        val percentIncrease = 0.1
        var level = 1

        while (leftExp > 0) {
            leftExp -= expReq
            if (leftExp >= 0 ) {level++}
            expReq += (expReq * percentIncrease).toInt()
        }

        return maxOf(1, level)
    }

    @SuppressLint("SetTextI18n")
    private fun updateExp() {
        val exp = databaseHelper.getUser("experience")
        val lvl = calculateLvl(exp).toString()

        if (oldLevel != 0 && lvl.toInt() > oldLevel) Toast.makeText(this, "You've just leveled up!", Toast.LENGTH_LONG).show()
        oldLevel = lvl.toInt()
        lvlTxt.text = "Level $lvl"
        expTxt.text = "EXP: $exp"
    }
}




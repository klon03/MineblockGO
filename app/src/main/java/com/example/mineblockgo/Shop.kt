package com.example.mineblockgo

import com.google.android.gms.maps.model.LatLng
import java.util.UUID

class Shop() {
    var id: String = UUID.randomUUID().toString()
    lateinit var position: LatLng private set
    fun addPosition(latLng: LatLng) {
        position = latLng
    }

    fun overwrite(id: String) {
        this.id = id
    }
}
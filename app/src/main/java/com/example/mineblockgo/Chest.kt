package com.example.mineblockgo

import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class Chest(val name: String, val description: String, val minGold: Int, val maxGold: Int, val isItems: Boolean, val simpleName: String) {
    var id: String = UUID.randomUUID().toString()
    lateinit var position: LatLng private set

    fun addPosition(latLng: LatLng) {
        position = latLng
    }

    fun overwrite(id: String) {
        this.id = id
    }
}

object ChestRepository {
    val chests = listOf(
        Chest("Chest", "Description description", 5, 50, false, "chest1")
    )
}
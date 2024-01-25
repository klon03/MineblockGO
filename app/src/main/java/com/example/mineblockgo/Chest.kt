package com.example.mineblockgo

import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class Chest(val name: String, val description: String, var minGold: Int, var maxGold: Int, val isItems: Boolean, val simpleName: String) {
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
        Chest("Normal chest", "Just a chest out in the wild. Nothing to be suspicious of.", 5, 50, false, "chest1"),
        Chest("Rare chest", "This chest has a suspicious glow. I wonder what could be inside.", 10, 60, false, "rarechest"),
        Chest("Legendary chest", "This chest seems to be storing something very important. I think we should check it out.", 30, 100, false, "legendarychest")
    )
}
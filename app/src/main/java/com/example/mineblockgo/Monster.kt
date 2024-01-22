package com.example.mineblockgo

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.UUID

data class Monster(val name: String, val description: String, val minStrength: Int, val maxStrength: Int) : Serializable {
    var strength: Int = (minStrength..maxStrength).random()
    var id: String = UUID.randomUUID().toString()
    lateinit var position: LatLng private set

    fun addPosition(latLng: LatLng) {
        position = latLng
    }

    fun overwrite(id: String, strength: Int) {
        this.id = id
        this.strength = strength
    }
}

object MonsterRepository {
    val monsters = listOf(
        Monster("Zombie", "Description description", 5, 15)
    )
}
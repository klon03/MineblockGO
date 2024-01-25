package com.example.mineblockgo

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.UUID

data class Monster(val name: String, val description: String, val minStrength: Int, val maxStrength: Int) : Serializable {
    var strength: Int = (minStrength..maxStrength).random()
    var id: String = UUID.randomUUID().toString()
    var startingStrength: Int = strength
    lateinit var position: LatLng private set

    fun addPosition(latLng: LatLng) {
        position = latLng
    }

    fun overwrite(id: String, strength: Int) {
        this.id = id
        this.strength = strength
        startingStrength = strength
    }

    fun dealDamage(dmg: Int) {
        strength = (strength - dmg).coerceAtLeast(0)
    }

    fun isDead(): Boolean {
        return strength <= 0
    }
}

object MonsterRepository {
    val monsters = listOf(
        Monster("Zombie", "The most popular monster out there. It should not be a challenge for you.", 5, 15),
        Monster("Skeleton", "A bit tougher than Zombie so watch out. It is harder to hit him also!", 10, 30),
        Monster("Enderman", "A beast from The End dimension! Tremendously hard to kill, good luck!", 40, 150)
    )
}
package com.example.mineblockgo

import android.graphics.Bitmap
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class LocationManager(private val context: Context, private val mMap: GoogleMap, private val currentLocationMarker: Marker) {
    private val monsters: MutableList<Monster> = mutableListOf()
    private val chests: MutableList<Chest> = mutableListOf()
    private val shops: MutableList<Shop> = mutableListOf()
    private val markers: MutableList<Marker> = mutableListOf()

    private val maxDistance: Int = 1500
    private val minDistance: Int = 100
    private val range: Int = 100

    var entityInRange: String? = null
    var entityInRangeType: MapActivity.MainButtonMode? = null
    private val databaseHelper = DatabaseManager.getDatabaseInstance()

    private val maxMonsters = 5
    private val maxChests = 2
    private val maxShops = 1

    // Ładowanie elementów z bazy danych i dodawanie ich do mapy (uruchamiane po załadowaniu mapy)
    fun loadEntitiesOnStartup() {
        val dbMonsters = databaseHelper.getAllMonsters()
        val dbChests = databaseHelper.getAllChests()
        val dbShops = databaseHelper.getAllShops()
        if (dbMonsters.isNotEmpty()) {
            loadEntitiesToMap("monster", dbMonsters)
        }
        if (dbChests.isNotEmpty()) {
            loadEntitiesToMap("chest", dbChests)
        }
        if (dbShops.isNotEmpty()) {
            loadEntitiesToMap("shop", dbShops)
        }
        checkEntitiesOnMove()
    }




    // Sprawdzanie czy w zasięgu gracza jest odpowiednia ilość elementów (uruchamiane przy każdej aktualizacji lokalizacji)
    fun checkEntitiesOnMove() {
        var monstersToAdd = maxMonsters
        var chestsToAdd = maxChests
        var shopsToAdd = maxShops

        monsters.forEach {
            if(isInDistance(it.position)){
                monstersToAdd--
            }
        }
        for (i in 1..monstersToAdd) {
            addNewEntity("monster")
        }

        chests.forEach {
            if(isInDistance(it.position)){
                chestsToAdd--
            }
        }
        for (i in 1..chestsToAdd) {
            addNewEntity("chest")
        }

        shops.forEach {
            if(isInDistance(it.position)){
                shopsToAdd--
            }
        }
        for (i in 1..shopsToAdd) {
            addNewEntity("shop")
        }
    }

    private fun addNewEntity(type: String) {
        val markerPosition = generateRandomLatLng(currentLocationMarker.position, minDistance, maxDistance)
        when(type) {
            "monster" -> {
                val randomMonster = MonsterRepository.monsters.random()
                val newMonster = Monster(randomMonster.name, randomMonster.description, randomMonster.minStrength, randomMonster.maxStrength)
                newMonster.addPosition(markerPosition)
                monsters.add(newMonster)
                databaseHelper.insertMonster(newMonster)
                addMarker(newMonster.name, 96, 160, newMonster.position, newMonster.name, monsterSnippet(newMonster), newMonster.id)
            }
            "chest" -> {
                val randomChest = ChestRepository.chests.random()
                val newChest = Chest(randomChest.name, randomChest.description, randomChest.minGold, randomChest.maxGold, randomChest.isItems, randomChest.simpleName)
                newChest.addPosition(markerPosition)
                chests.add(newChest)
                databaseHelper.insertChest(newChest)
                addMarker(newChest.simpleName, 115, 115,newChest.position, newChest.name, chestSnippet(newChest), newChest.id)
            }
            "shop" -> {
                val newShop = Shop()
                newShop.addPosition(markerPosition)
                shops.add(newShop)
                databaseHelper.insertShop(newShop)
                addCircle(newShop.position, newShop.id)
            }
        }
    }






    // Ładowanie listy elementów na mapę
    private fun loadEntitiesToMap(entityType: String, entities: List<Any>) {
        entities.forEach {
            when (entityType) {
                "monster" -> {
                    val monster = it as Monster
                    monsters.add(monster)
                    addMarker(monster.name, 96, 160, monster.position, monster.name, monsterSnippet(monster), monster.id)
                }
                "chest" -> {
                    val chest = it as Chest
                    chests.add(chest)
                    addMarker(chest.simpleName, 115, 115, chest.position, chest.name, chestSnippet(chest), chest.id)
                }
                "shop" -> {
                    val shop = it as Shop
                    shops.add(shop)
                    addCircle(shop.position, shop.id)
                }
            }
        }
    }

    private fun monsterSnippet(mob: Monster) : String {
        return "${mob.minStrength} - ${mob.maxStrength} strength"
    }

    private fun chestSnippet(chest: Chest) : String {
        return chest.minGold.toString() + " - " + chest.maxGold.toString() + " gold"
    }

    private fun addMarker(imgName: String, imgWidth: Int, imgHeight: Int, position: LatLng, title: String, snippet: String, tag: String) {
        val imageResource = context.resources.getIdentifier(imgName.lowercase(), "drawable", context.packageName)
        val customMarkerBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, imageResource)

        val scaledBitmap = Bitmap.createScaledBitmap(customMarkerBitmap, imgWidth, imgHeight, false)
        val customMarkerIcon: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        val marker =
            mMap.addMarker(
                MarkerOptions().position(position).title(title).icon(customMarkerIcon).snippet(snippet)
            )
        marker!!.tag = tag
        markers.add(marker)
    }

    private fun addCircle(position: LatLng, tag: String) {

        val circleOptions = CircleOptions()
            .center(position)
            .radius(100.0)
            .strokeWidth(3f)
            .strokeColor(Color.GREEN)
            .fillColor(Color.argb(60, 3, 201, 0))

        val circle = mMap.addCircle(circleOptions)
        circle.tag = tag
    }

    private fun generateRandomLatLng(baseLatLng: LatLng, radiusMin: Int, radiusMax: Int): LatLng {
        var randomLatLng: LatLng

        do {
            val randomRadius = (radiusMin..radiusMax).random()
            val randomAngle = Math.toRadians(Math.random() * 360)

            val offsetX = randomRadius * cos(randomAngle)
            val offsetY = randomRadius * sin(randomAngle)

            val newLat = baseLatLng.latitude + offsetY / 111111.0
            val newLng = baseLatLng.longitude + offsetX / (111111.0 * cos(Math.toRadians(baseLatLng.latitude)))

            randomLatLng = LatLng(newLat, newLng)
        } while (isInShopRadius(randomLatLng, shops))

        return randomLatLng
    }

    private fun isInShopRadius(testLatLng: LatLng, shops: List<Shop>): Boolean {
        val shopRadius = 100.0

        for (shop in shops) {
            val distance = calculateDistance(testLatLng, shop.position)
            if (distance < shopRadius) {
                return true
            }
        }

        return false
    }


    private fun calculateDistance(a: LatLng, b: LatLng): Int {
        val R = 6371000.0 // Promień Ziemi w metrach

        val lat1 = Math.toRadians(a.latitude)
        val lon1 = Math.toRadians(a.longitude)
        val lat2 = Math.toRadians(b.latitude)
        val lon2 = Math.toRadians(b.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return (R * c).roundToInt()
    }

    // Sprawdzanie czy cokolwiek jest w zasięgu interakcji i zwracanie docelowego stanu przycisku
    fun checkVicinity(): MapActivity.MainButtonMode {
        var closestTag: String? = null
        var closestDistance: Int = -1
        var closestType: MapActivity.MainButtonMode? = null

        for(mob in monsters) {
            val distance = calculateDistance(mob.position, currentLocationMarker.position)
            if (distance <= range) {
                if (closestTag == null || closestDistance > distance) {
                    closestTag = mob.id
                    closestDistance = distance
                    closestType = MapActivity.MainButtonMode.COMBAT
                }
            }
        }

        for(chest in chests) {
            val distance = calculateDistance(chest.position, currentLocationMarker.position)
            if (distance <= range) {
                if (closestTag == null || closestDistance > distance) {
                    closestTag = chest.id
                    closestDistance = distance
                    closestType = MapActivity.MainButtonMode.CHEST
                }
            }
        }

        for(shop in shops) {
            val distance = calculateDistance(shop.position, currentLocationMarker.position)
            if (distance <= range) {
                if (closestTag == null || closestDistance > distance) {
                    closestTag = shop.id
                    closestDistance = distance
                    closestType = MapActivity.MainButtonMode.SHOP
                }
            }
        }


        if (closestType == null) {
            closestType = MapActivity.MainButtonMode.DEFAULT
        }
        entityInRange = closestTag
        entityInRangeType = closestType
        return closestType
    }


    private fun isInDistance(entityPosition: LatLng) : Boolean {
        val distance = calculateDistance(entityPosition, currentLocationMarker.position)
        return distance <= maxDistance
    }

    fun deleteEntity(tag: String, table: String) {
        removeMarker(tag)

        when (table) {
            "monsters" -> {
                val foundMonster = monsters.find { it.id == tag }
                if (monsters.removeIf { it.id == tag }) {
                    Log.w("fdf", "usunieto potwora")
                }
            }
            "chests" -> {
                chests.removeIf { it.id == tag }
            }
        }

        databaseHelper.deleteRowByTag(table, tag)
        entityInRange = null
        entityInRangeType = null
    }

    private fun removeMarker(tag: String) {
        val foundMarker = markers.find { it.tag == tag }
        foundMarker?.remove()
        markers.remove(foundMarker)

    }
}
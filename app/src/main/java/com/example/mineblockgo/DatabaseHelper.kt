package com.example.mineblockgo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.android.gms.maps.model.LatLng

object DatabaseManager {
    private var instance: DatabaseHelper? = null

    fun initialize(context: Context) {
        if (instance == null) {
            instance = DatabaseHelper(context)
        }
    }

    fun getDatabaseInstance(): DatabaseHelper {
        return instance ?: throw IllegalStateException("DatabaseManager must be initialized")
    }
}

const val DATABASE_NAME = "main_database"
const val DATABASE_VERSION = 1
class DatabaseHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Tworzenie tabeli przy pierwszym uruchomieniu bazy danych
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS monsters " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, tag TEXT, strength INTEGER, lat REAL, lng REAL)"
        )
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS chests " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, simple_name TEXT, tag TEXT, lat REAL, lng REAL)"
        )
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS shops " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, tag TEXT, lat REAL, lng REAL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun insertMonster(monster: Monster) {
        val values = ContentValues().apply {
            put("name", monster.name)
            put("tag", monster.id)
            put("strength", monster.strength)
            put("lat", monster.position.latitude)
            put("lng", monster.position.longitude)
        }

        writableDatabase.insert("monsters", null, values)
    }

    fun insertChest(chest: Chest) {
        val values = ContentValues().apply {
            put("simple_name", chest.simpleName)
            put("tag", chest.id)
            put("lat", chest.position.latitude)
            put("lng", chest.position.longitude)
        }

        writableDatabase.insert("chests", null, values)
    }

    fun insertShop(shop: Shop) {
        val values = ContentValues().apply {
            put("tag", shop.id)
            put("lat", shop.position.latitude)
            put("lng", shop.position.longitude)
        }

        writableDatabase.insert("shops", null, values)
    }

    fun getAllMonsters(): List<Monster> {
        val monsterList = mutableListOf<Monster>()

        val query = "SELECT * FROM monsters"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                val name = it.getString(1)
                val id = it.getString(2)
                val strength = it.getInt(3)
                val latitude = it.getDouble(4)
                val longitude = it.getDouble(5)

                val template = MonsterRepository.monsters.find { monster -> monster.name == name }
                if (template != null) {
                    val monster = Monster(name,template.description, template.minStrength, template.maxStrength)
                    monster.addPosition(LatLng(latitude, longitude))
                    monster.overwrite(id, strength)
                    monsterList.add(monster)
                }
            }
        }

        return monsterList
    }

    fun getAllChests(): List<Chest> {
        val chestList = mutableListOf<Chest>()

        val query = "SELECT * FROM chests"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                val simpleName = it.getString(1)
                val id = it.getString(2)
                val latitude = it.getDouble(3)
                val longitude = it.getDouble(4)

                val template = ChestRepository.chests.find { chest -> chest.simpleName == simpleName }
                if (template != null) {
                    val chest = Chest(template.name, template.description, template.minGold, template.maxGold, template.isItems, simpleName)
                    chest.addPosition(LatLng(latitude, longitude))
                    chest.overwrite(id)
                    chestList.add(chest)
                }
            }
        }

        return chestList
    }

    fun getAllShops(): List<Shop> {
        val shopList = mutableListOf<Shop>()

        val query = "SELECT * FROM shops"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getString(1)
                val latitude = it.getDouble(2)
                val longitude = it.getDouble(3)

                val shop = Shop()
                shop.addPosition(LatLng(latitude, longitude))
                shop.overwrite(id)
                shopList.add(shop)

            }
        }

        return shopList
    }

    fun selectMonster(tag: String): Monster? {
        val db = this.readableDatabase
        var monster: Monster? = null
        val cursor = db.rawQuery("SELECT * FROM monsters WHERE tag = ?", arrayOf(tag))

        cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(1)
                val tag = cursor.getString(2)
                val strength = cursor.getString(3).toInt()  // Konwersja na Int
                val lat = cursor.getString(4).toDouble()    // Konwersja na Double
                val lng = cursor.getString(5).toDouble()    // Konwersja na Double

                val template = MonsterRepository.monsters.find { it.name == name }
                if (template != null) {
                    monster = Monster(
                        name = name,
                        description = template.description,
                        minStrength = template.minStrength,
                        maxStrength = template.maxStrength
                    )
                    monster!!.addPosition(LatLng(lat, lng))
                    monster!!.overwrite(tag, strength)
                }
            }
        }

        return monster
    }

    fun selectChest(tag: String): Chest? {
        val db = this.readableDatabase
        var chest: Chest? = null
        val cursor = db.rawQuery("SELECT * FROM chests WHERE tag = ?", arrayOf(tag))

        cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val simpleName = cursor.getString(1)
                val tag = cursor.getString(2)
                val lat = cursor.getString(3).toDouble()
                val lng = cursor.getString(4).toDouble()

                val template = ChestRepository.chests.find { it.simpleName == simpleName }
                if (template != null) {
                    chest = Chest(
                        name = template.name,
                        description = template.description,
                        minGold = template.minGold,
                        maxGold = template.maxGold,
                        isItems = template.isItems,
                        simpleName = simpleName
                    )
                    chest!!.addPosition(LatLng(lat, lng))
                    chest!!.overwrite(tag)
                }
            }
        }

        return chest
    }
}
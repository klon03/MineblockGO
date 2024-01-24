package com.example.mineblockgo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.example.mineblockgo.objects.Weapon

import android.util.Log

import com.google.android.gms.maps.model.LatLng
import java.sql.SQLException

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
        db?.execSQL(

            "CREATE TABLE IF NOT EXISTS items " +
                    "(id INTEGER PRIMARY KEY, name TEXT, iconID INTEGER, endurance INTEGER, dmg INTEGER)"
        )
        db?.execSQL(
            "CREATE TABLE IF NOT EXISTS user " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, value_int INTEGER);"
        )
        db?.execSQL("INSERT INTO user (name, value_int) VALUES ('gold', 50)")
        db?.execSQL("INSERT INTO user (name, value_int) VALUES ('experience', 0)")

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

    fun insertItem(item: Weapon): Boolean{
        var weaponList = getAllItems()
        for (i in 0..8){
            val containsObjectWithId = weaponList.any { it.wpID == i }

            if (!containsObjectWithId) {
                item.wpID = i
                val values = ContentValues().apply {
                    put("id", item.wpID)
                    put("name", item.name)
                    put("iconID", item.iconId)
                    put("endurance", item.endurance)
                    put("dmg", item.damage)
                }

                writableDatabase.insert("items", null, values)
                return true
            }

        }
        return false
    }

    fun removeItem(itemId: Int) { //usuwanie itemka z bazy
        writableDatabase.delete("items", "id = ?", arrayOf(itemId.toString()))
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


    fun getAllItems(): List<Weapon>{
        val itemList = mutableListOf<Weapon>()

        val query = "SELECT * FROM items"
        val db = this.readableDatabase
        val cursor = db.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(0)
                val name = it.getString(1)
                val iconID = it.getInt(2)
                val endurance = it.getInt(3)
                val dmg = it.getInt(4)
                val weapon = Weapon(id, name, iconID, endurance, dmg, 0)
                itemList.add(weapon)

            }
        }

        return itemList
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

    fun deleteRowByTag(tableName: String, tag: String): Boolean {
        val db = writableDatabase

        return try {
            val affectedRows = db.delete(tableName, "tag=?", arrayOf(tag))
            affectedRows > 0
        } catch (e: SQLException) {
            false
        }
    }

    fun getUser(option: String): Int {
        val db = readableDatabase
        val cursor = db?.rawQuery("SELECT value_int FROM user WHERE name = ?", arrayOf(option))

        return if (cursor != null && cursor.moveToFirst()) {
            val level = cursor.getInt(0)
            cursor.close()
            Log.w("ww", "pobrano $level")
            level
        } else {
            cursor?.close()
            -1
        }
    }

    fun updateUser(option: String, value: Int) {
        val db = this.writableDatabase
        Log.w("ww", "przakazano $value")
        val currentExp = getUser(option)
        val newExp = maxOf(currentExp + value, 0)
        Log.w("ww", "dodano $newExp")
        val contentValues = ContentValues()
        contentValues.put("value_int", newExp)
        db.update("user", contentValues, "name=?", arrayOf(option))
    }
}
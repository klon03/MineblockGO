package com.example.mineblockgo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class CombatActivity : AppCompatActivity() {
    private val databaseHelper = DatabaseManager.getDatabaseInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combat)


        val tag = intent.getStringExtra("tag")
//        val monster = databaseHelper.selectMonster(tag)
    }
}
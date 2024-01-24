package com.example.mineblockgo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class ChestActivity : AppCompatActivity() {
    private val databaseHelper = DatabaseManager.getDatabaseInstance()
    private val tag: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chest)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tag = intent.getStringExtra("tag")
        val chestOpen: Button = findViewById(R.id.chestOpen)

        chestOpen.setOnClickListener {
            openChest()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openChest() {
        val reward = (5..50).random()
        val goldAmount: TextView = findViewById(R.id.goldAmount)
        goldAmount.text = "Gold: $reward"

        AlertDialog.Builder(this)
            .setTitle("Chest")
            .setMessage("You got $reward gold!")
            .setPositiveButton("OK") { _, _ ->
                val resultIntent = Intent()
                resultIntent.putExtra("chestTag", tag)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
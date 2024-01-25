package com.example.mineblockgo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import android.widget.ImageButton
import android.widget.ImageView

class ChestActivity : AppCompatActivity() {
    private val databaseHelper = DatabaseManager.getDatabaseInstance()

    private lateinit var chest: Chest
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chest)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Pobranie obiektu skrzynki
        val tag = intent.getStringExtra("tag") ?: ""
        chest = databaseHelper.selectChest(tag) ?: run {
            finish()
            return
        }

        val chestLoot: TextView = findViewById(R.id.chestLoot)
        chestLoot.text = "${chest.minGold} - ${chest.maxGold}"

        val chestOpen: Button = findViewById(R.id.chestOpen)
        val chestName: TextView = findViewById(R.id.chestName)
        chestName.text = chest.name
        val chestDescription: TextView = findViewById(R.id.chestDescription)
        chestDescription.text = chest.description

        val imageResource = resources.getIdentifier(chest.simpleName.lowercase(), "drawable", packageName)
        val chestImage : ImageView = findViewById(R.id.chestImage)
        chestImage.setImageResource(imageResource)

        chestOpen.setOnClickListener {
            openChest()
        }

        val backBtn: ImageButton = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
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
        val reward = (chest.minGold..chest.maxGold).random()
        val goldAmount: TextView = findViewById(R.id.goldAmount)
        goldAmount.text = "Gold: $reward"
        databaseHelper.updateUser("gold", reward)

        AlertDialog.Builder(this)
            .setTitle("Chest")
            .setMessage("You got $reward gold!")
            .setPositiveButton("OK") { _, _ ->
                val resultIntent = Intent()
                resultIntent.putExtra("tag", chest.id)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
package com.example.mineblockgo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mineblockgo.objects.Weapon
import android.widget.ImageButton
import android.widget.Button
import android.widget.TextView


class EQActivity : AppCompatActivity() {
    private val databaseHelper = DatabaseManager.getDatabaseInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eq)
        val gold = databaseHelper.getUser("gold")
        val goldTxt : TextView = findViewById(R.id.goldTxt)
        goldTxt.text = "$gold"



        val imageButton0 = findViewById<ImageButton>(R.id.imageButton0)
        val imageButton1 = findViewById<ImageButton>(R.id.imageButton1)
        val imageButton2 = findViewById<ImageButton>(R.id.imageButton2)
        val imageButton3 = findViewById<ImageButton>(R.id.imageButton3)
        val imageButton4 = findViewById<ImageButton>(R.id.imageButton4)
        val imageButton5 = findViewById<ImageButton>(R.id.imageButton5)
        val imageButton6 = findViewById<ImageButton>(R.id.imageButton6)
        val imageButton7 = findViewById<ImageButton>(R.id.imageButton7)
        val imageButton8 = findViewById<ImageButton>(R.id.imageButton8)
        val imageButtons = listOf(imageButton0, imageButton1, imageButton2, imageButton3, imageButton4, imageButton5, imageButton6, imageButton7, imageButton8)

        val eq = Equipment(this, imageButtons)

        eq.setEquipmentButtons()
        eq.setBtnImage()

        eq.setWeaponImageInImageView(0)


        val exit = findViewById<ImageButton>(R.id.exit)

        exit.setOnClickListener{
            finish()
        }
    }

}
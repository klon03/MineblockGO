package com.example.mineblockgo

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity



class CombatActivity : AppCompatActivity() {

    private val databaseHelper = DatabaseManager.getDatabaseInstance()
    private lateinit var monster: Monster

    private lateinit var backBtn : ImageButton
    private lateinit var fightBtn : ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var mobNameTxt: TextView
    private lateinit var mobDescriptionTxt: TextView
    private lateinit var mobImage: ImageView
    private lateinit var mobPower: TextView

    private var isTimerRunning = false
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combat)

        // Pobranie danych potwora
        val tag = intent.getStringExtra("tag") ?: ""
        monster = databaseHelper.selectMonster(tag) ?: run {
            finish()
            return
        }

        // Edycja widoku
        initUi()

        // Wybór broni

        // Start walki
        fightBtn.setOnClickListener {
            if (!isTimerRunning) {
                startCombat()
            } else {
                dealDamage()
            }
        }




        // Pobranie listy broni razem z statystykami

        // onclick kalkulacja walki

        // po walce aktualizacja BD -> nowa aktywność
    }

    private fun initUi() {
        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
        mobNameTxt = findViewById(R.id.monsterName)
        mobDescriptionTxt = findViewById(R.id.monsterDescription)
        mobImage = findViewById(R.id.monsterImage)
        mobPower = findViewById(R.id.monsterPower)
        fightBtn = findViewById(R.id.fightBtn)
        progressBar = findViewById(R.id.progressBar)

        mobNameTxt.text = monster.name
        mobDescriptionTxt.text = monster.description
        val imageResource = resources.getIdentifier(monster.name.lowercase(), "drawable", packageName)
        mobImage.setImageResource(imageResource)
        mobPower.text = getString(R.string.strength_range, monster.minStrength.toString(), monster.maxStrength.toString())
    }

    private fun startCombat() {
        isTimerRunning = true

        mobPower.text = monster.strength.toString()
        mobPower.setTextColor(Color.RED)

        timer = object : CountDownTimer(5000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                progressBar.progress = millisUntilFinished.toInt()
            }

            override fun onFinish() {
                // Koniec czasu
                endFight(false)
            }
        }

        timer?.start()
    }

    private fun dealDamage() {
        val damage = 5 // Do zmiany na dmg broni
        monster.dealDamage(damage)
        mobPower.text = monster.strength.toString()
        if (monster.isDead()) {
            endFight(true)
        }
    }

    private fun endFight(isPlayerVictorious: Boolean) {
        timer?.cancel()
        isTimerRunning = false

        val builder = AlertDialog.Builder(this)

        if (isPlayerVictorious) {
            // usuwanie potwora z bd
            // zwrot kodu do usunięcia potwora z mapy
            // dodawanie expa

            builder.setTitle("You won!")
                .setMessage("You have slain the monster and gained some experience!")
                .setPositiveButton("OK") { dialog, _ ->

                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
        } else {
            // odejmowanie expa

            builder.setTitle("You lost!")
                .setMessage("The monster was better this time! You lost some experience.")
                .setPositiveButton("OK") { dialog, _ ->

                    dialog.dismiss()
                    val resultIntent = Intent()
                    resultIntent.putExtra("monsterTag", monster.id)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
                .setCancelable(false)
        }

        val dialog = builder.create()
        dialog.show()
    }
}
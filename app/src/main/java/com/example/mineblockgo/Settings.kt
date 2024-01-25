package com.example.mineblockgo


import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Settings : AppCompatActivity() {

    private val themeTitleList = arrayOf("Light", "Dark", "Auto")
    private lateinit var restart: Button
    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val changeThemeBtn = findViewById<Button>(R.id.changeThemeBtn)
        restart = findViewById(R.id.restartBtn)


        val sharedPreferenceManger = SharedPreferenceManger(this)
        var checkedTheme = sharedPreferenceManger.theme
        changeThemeBtn.text = "Theme: ${themeTitleList[sharedPreferenceManger.theme]}"

        val themeDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Theme")
            .setPositiveButton("Ok") { _, _ ->
                sharedPreferenceManger.theme = checkedTheme
                AppCompatDelegate.setDefaultNightMode(sharedPreferenceManger.themeFlag[checkedTheme])
                changeThemeBtn.text = "Theme: ${themeTitleList[sharedPreferenceManger.theme]}"
            }
            .setSingleChoiceItems(themeTitleList, checkedTheme) { _, which ->
                checkedTheme = which
            }
            .setCancelable(false)

        changeThemeBtn.setOnClickListener {
            themeDialog.show()
        }

        restart.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            startActivity(intent)
            overridePendingTransition(0, 0)
            Toast.makeText(this, "App has been reset", Toast.LENGTH_LONG).show()
        }
    }
}

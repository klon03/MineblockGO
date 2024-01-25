package com.example.mineblockgo

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mineblockgo.objects.Weapon


class CombatActivity : AppCompatActivity() {

    private val databaseHelper = DatabaseManager.getDatabaseInstance()
    private lateinit var monster: Monster
    private lateinit var equipment: List<Weapon>
    private var weaponOptions: MutableList<WeaponOption> = mutableListOf()
    private var damage: Int = 0
    private var weaponId: Int? = null

    private lateinit var backBtn : ImageButton
    private lateinit var fightBtn : ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var mobNameTxt: TextView
    private lateinit var mobDescriptionTxt: TextView
    private lateinit var mobImage: ImageView
    private lateinit var mobPower: TextView
    private lateinit var eqSpinner: Spinner

    private var isTimerRunning = false
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_combat)

        // Pobranie ekwipunku
        equipment = databaseHelper.getAllItems()
        equipment.forEach {
            weaponOptions.add(WeaponOption(it))
        }

        // Pobranie danych potwora
        val tag = intent.getStringExtra("tag") ?: ""
        monster = databaseHelper.selectMonster(tag) ?: run {
            finish()
            return
        }
        // Edycja widoku
        initUi()


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
        eqSpinner = findViewById(R.id.spinnerEq)

        mobNameTxt.text = monster.name
        mobDescriptionTxt.text = monster.description
        val imageResource = resources.getIdentifier(monster.name.lowercase(), "drawable", packageName)
        mobImage.setImageResource(imageResource)
        mobPower.text = buildString {
            append(monster.minStrength.toString())
            append(" - ")
            append(monster.maxStrength.toString())
        }


        // Spinner
        val adapter = WeaponOptionAdapter(this, weaponOptions)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        eqSpinner.adapter = adapter

        eqSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = parent?.getItemAtPosition(position) as? WeaponOption
                if (selectedOption != null) {
                    damage = selectedOption.dmg
                    weaponId = selectedOption.id

                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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

        weaponId?.let { databaseHelper.updateItem(it) }

        if (isPlayerVictorious) {
            builder.setTitle("You won!")
                .setMessage("You have slain the monster and gained some experience!")
                .setPositiveButton("OK") { dialog, _ ->
                    databaseHelper.updateUser("experience", monster.startingStrength)
                    val resultIntent = Intent()
                    resultIntent.putExtra("tag", monster.id)
                    setResult(Activity.RESULT_OK, resultIntent)
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
        } else {

            builder.setTitle("You lost!")
                .setMessage("The monster was better this time! You lost some experience.")
                .setPositiveButton("OK") { dialog, _ ->
                    databaseHelper.updateUser("experience", -monster.strength)
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
        }

        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        isTimerRunning = false
        timer?.cancel()
    }
}

data class WeaponOption(val weapon: Weapon) {
    val displayText = "${weapon.name} ${weapon.damage}DMG (${weapon.endurance} uses left)"
    val dmg = weapon.damage
    val id = weapon.wpID
}

class WeaponOptionAdapter(context: Context, options: List<WeaponOption>) :
    ArrayAdapter<WeaponOption>(context, android.R.layout.simple_spinner_item, options) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val option = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = option?.displayText
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val option = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = option?.displayText
        return view
    }
}
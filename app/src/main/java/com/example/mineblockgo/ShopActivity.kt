package com.example.mineblockgo

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StrikethroughSpan
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mineblockgo.objects.Weapon

class ShopActivity: AppCompatActivity() {
    private lateinit var shopTextView: TextView
    private lateinit var currencyTextView: TextView
    private lateinit var shopLayout: LinearLayout
    private lateinit var specialOfferLayout: LinearLayout
    private lateinit var backButton: ImageButton

    private val databaseHelper = DatabaseManager.getDatabaseInstance()

    private var playerCurrency = databaseHelper.getUser("gold")
    //  lista naszych przedmiotów
    private val weaponsList = listOf(
        Weapon(1,"Wooden Sword",R.drawable.wooden_sword,5,3,15),
        Weapon(1,"Stone Sword",R.drawable.stone_sword,10,6,30),
        Weapon(1,"Golden Sword",R.drawable.golden_sword,5,20,50),
        Weapon(1,"Iron Sword",R.drawable.iron_sword,15,12,70),
        Weapon(1,"Diamond Sword",R.drawable.diamond_sword2,20,20,99)
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        shopTextView = findViewById(R.id.shopText)
        currencyTextView = findViewById(R.id.playersGold)
        shopLayout = findViewById(R.id.itemsLayout)
        specialOfferLayout = findViewById(R.id.specialOfferLayout)
        backButton = findViewById(R.id.backImageButton)

        updatePlayersGold()
        generateSpecialOffer()
        generateShopWeapons()

        backButton.setOnClickListener{
            finish()
        }
    }

    //  funkcja na update naszego golda
    @SuppressLint("SetTextI18n")
    private fun updatePlayersGold(){
        currencyTextView.text = "$playerCurrency"
    }
    //   funkcja na generowanie specjalnej oferty w sklepie
    private fun generateSpecialOffer(){
        val weapon = weaponsList.random()
        createSpecialOfferView(weapon)
    }

    @SuppressLint("SetTextI18n")
    fun createSpecialOfferView(weapon: Weapon) {
//      tworzenie layoutu special offer
        val specialOfferTextLayout = LinearLayout(this)
        specialOfferTextLayout.orientation = LinearLayout.VERTICAL
        specialOfferTextLayout.setPadding(5,5,5,20)

//      tworzenie tekstu SPECIAL OFFER i dodanie go do glownego layoutu Special Offer
        val specialOfferTextView = TextView(this)
        specialOfferTextView.text = "Special Offer"
        specialOfferTextView.textAlignment = LinearLayout.TEXT_ALIGNMENT_CENTER
        specialOfferTextView.textSize = 30f

        specialOfferTextLayout.addView(specialOfferTextView)
        startColorTextAnimation(specialOfferTextView)

//      tworzenie broni ktora sie pojawi w layoucie special offer (pod layoutem SpecialOfferTextView)
//      tworzenie layoutu broni
        val specialOfferWeaponLayout = LinearLayout(this)
        specialOfferWeaponLayout.orientation = LinearLayout.VERTICAL
        specialOfferWeaponLayout.setPadding(0,0,0,20)

//      tworzenie layoutu ikonki w layoucie broni SpecialOfferWeaponLayout
        val specialOfferIconImageView = ImageView(this)
        specialOfferIconImageView.setImageResource(weapon.iconId)

        specialOfferWeaponLayout.addView(specialOfferIconImageView)

//      tworzenie layoutu info o broni obok layoutu ikonki w layoucie SpecialOfferWeaponLayout
        val specialOfferWeaponInfoLayout = LinearLayout(this)
        specialOfferWeaponInfoLayout.orientation = LinearLayout.VERTICAL
        specialOfferWeaponInfoLayout.setPadding(10,0,0,20)

//      tworzenie TextView z nazwą broni i dodanie go do layoutu SpecialOfferWeaponInfoLayout
        val specialOfferWeaponNameTextView = TextView(this)
        specialOfferWeaponNameTextView.text = weapon.name
        specialOfferWeaponNameTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        specialOfferWeaponInfoLayout.addView(specialOfferWeaponNameTextView)

//      tworzenie TextView z statami broni i dodanie go do layoutu SpecialOfferWeaponInfoLayout
        val specialOfferWeaponStatsTextView = TextView(this)
        specialOfferWeaponStatsTextView.text = "Endurance: ${weapon.endurance} | Damage: ${weapon.damage}"
        specialOfferWeaponStatsTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        specialOfferWeaponInfoLayout.addView(specialOfferWeaponStatsTextView)

//      dodanie layoutu z info i statami do layoutu calej broni w ktorej juz byla ikonka, a teraz jest wszystko
        specialOfferWeaponLayout.addView(specialOfferWeaponInfoLayout)

//      dodanie przycisku za pomoca ktorego kupimy nasza przeceniona bron
        val specialOfferBuyButton = Button(this)

        specialOfferBuyButton.text = getSpecialPrice(weapon.price)
        weapon.wpID = 0
        specialOfferBuyButton.setOnClickListener{
//           kupno specjalnej broni
            weapon.price = (0.7*weapon.price).toInt()
            buyWeapon(weapon, specialOfferBuyButton)
        }

//     dodanie przycisku do layoutu SpecialOfferWeaponLayout
        specialOfferWeaponLayout.addView(specialOfferBuyButton)


//      dodanie layoutow z tekstem SPECIAL OFFER oraz layoutu z sama bronia (ikonka, nazwa, staty i przycisk) do glowengo layoutu ktory bedzie generowany
        specialOfferLayout.addView(specialOfferTextLayout)
        specialOfferLayout.addView(specialOfferWeaponLayout)
    }
    //  funkcja na generowanie itemów w sklepie
    private fun generateShopWeapons(){
        for (i in 1..3){
            val weapon = weaponsList.random()
            createWeaponsView(weapon, i)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createWeaponsView(weapon: Weapon, n:Int){
        val weaponLayout = LinearLayout(this)
        weaponLayout.orientation = LinearLayout.VERTICAL
        weaponLayout.setPadding(0,0,0,20)

        val iconImageView = ImageView(this)
        iconImageView.setImageResource(weapon.iconId)
        weaponLayout.addView(iconImageView)

        val weaponInfoLayout = LinearLayout(this)
        weaponInfoLayout.orientation = LinearLayout.VERTICAL
        weaponInfoLayout.setPadding(10,0,0,20)

        val weaponNameTextView = TextView(this)
        weaponNameTextView.text = weapon.name
        weaponNameTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        weaponInfoLayout.addView(weaponNameTextView)

        val weaponStatsTextView = TextView(this)
        weaponStatsTextView.text = "Endurance: ${weapon.endurance} | Damage: ${weapon.damage}"
        weaponStatsTextView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        weaponInfoLayout.addView(weaponStatsTextView)

        weaponLayout.addView(weaponInfoLayout)

        val buyButton = Button(this)
        buyButton.text = "${weapon.price} coins"
        weapon.wpID = n
        buyButton.setOnClickListener{
//            kupno broni

            buyWeapon(weapon, buyButton)

        }
        weaponLayout.addView(buyButton)

        shopLayout.addView(weaponLayout)
    }

    //  funkcja pozwalajaca kupic bron
    @SuppressLint("SetTextI18n")
    private fun buyWeapon(wp:Weapon, button: Button){
        var price = wp.price.toDouble()
        if (playerCurrency >= price){
            playerCurrency -= price.toInt()
            if (databaseHelper.insertItem(wp))
            {
                databaseHelper.updateUser("gold", -price.toInt())
                updatePlayersGold()
                button.text = "SOLD OUT"
                button.isEnabled = false
            }
            else{
                notEnoughSpaceInEq()
            }
        }
        else{
            notEnoughGoldAlert()
        }
    }
    //  funkcja ktora wywoluje sie gdy chcemy kupic przedmiot na ktory nas nie stac

    private fun notEnoughSpaceInEq(){
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("Error!")
        alertBuilder.setMessage("You don have enough space in eq!")
        alertBuilder.setPositiveButton("ok"){
                dialog, _ -> dialog.dismiss()
        }
        val alert = alertBuilder.create()
        alert.show()
    }
    private fun notEnoughGoldAlert(){
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle("Error!")
        alertBuilder.setMessage("You don have enough gold!")
        alertBuilder.setPositiveButton("I'm gonna get more!"){
                dialog, _ -> dialog.dismiss()
        }
        val alert = alertBuilder.create()
        alert.show()
    }
    //  funkcja, ktora generuje tekst dla przedmiotow specjalnych i przekresla stara cene
    private fun getSpecialPrice(oldPrice:Int): SpannableStringBuilder {
        val previousPrice = "$oldPrice coins"
        val newPrice = oldPrice*0.7
        val newPriceText = "   ${newPrice.toInt()} coins"
        val spannableStringOld = SpannableString(previousPrice)
        val spannableStringNew = SpannableString(newPriceText)
        spannableStringOld.setSpan(StrikethroughSpan(), 0,previousPrice.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val spannableStringBuilder = SpannableStringBuilder()
        spannableStringBuilder.append(spannableStringOld)
        spannableStringBuilder.append(spannableStringNew)
        return spannableStringBuilder
    }
    // funkcja do modyfikacji tekstu jako kolorowa animacja
    private fun startColorTextAnimation(textView: TextView){
        val colorStart = Color.parseColor("#FFD700")
        val colorMid = Color.parseColor("#8338EC")
        val colorMid2 = Color.parseColor("#3A86FF")
        val colorEnd = Color.parseColor("#FF4500")

        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorStart, colorMid, colorMid2, colorEnd)
        colorAnimator.repeatCount = ValueAnimator.INFINITE
        colorAnimator.duration = 2000

        colorAnimator.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            textView.setTextColor(animatedColor)
        }

        colorAnimator.start()
    }

}
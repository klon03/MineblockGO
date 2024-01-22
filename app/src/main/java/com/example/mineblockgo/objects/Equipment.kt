package com.example.mineblockgo

import androidx.appcompat.app.AppCompatActivity
import com.example.mineblockgo.objects.Weapon
import android.widget.ImageButton
import android.content.Context
import android.app.Activity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class Equipment(private val context: Context, private val imageButtons: List<ImageButton>): AppCompatActivity(){
    private val defaultImage = R.drawable.default_image
    private val weapons = arrayOfNulls<Weapon>(9)
    private var selected = 0

    private fun setBtnImage(index: Int, imageResId: Int) { // to jest pojebane ale działa
        val imageButtonId = context.resources.getIdentifier("imageButton$index", "id", context.packageName)
        val imageButton = (context as? Activity)?.findViewById<ImageButton>(imageButtonId)
        imageButton?.setImageResource(imageResId)
    }

    fun addWeaponToEquipment(newWeapon: Weapon) { //tutaj sobie adam dodajesz obiekt weapon do eq jak już sobie ziomek kupi
        for (i in weapons.indices) {
            if (weapons[i] == null) {
                weapons[i] = newWeapon
                setBtnImage(i, newWeapon.iconId)
                return
            }
        }

        println("full") //jak sie miejsce skończy
    }

    private fun removeWeapon(rmWeaponIndex: Int){
        weapons[rmWeaponIndex] = null
    }

    fun setEquipmentButtons() {
        imageButtons.forEachIndexed { index, imageButton ->
            imageButton.setOnClickListener {
                setWeaponImageInImageView(index)
            }
        }
    }

    fun setWeaponImageInImageView(index: Int) {
        if (index in 0 until weapons.size) {
            selected = index
            val selectedWeapon = weapons[index]
            val imageView = (context as? Activity)?.findViewById<ImageView>(R.id.imageView)
            val textView = (context as? Activity)?.findViewById<TextView>(R.id.textView)
            if(selectedWeapon != null) {
                imageView?.setImageResource(selectedWeapon.iconId)
                textView?.text = "${selectedWeapon.name} \nATK: ${selectedWeapon.damage} \nDurability: ${selectedWeapon.endurance}"
            }
            else {
                imageView?.setImageResource(defaultImage)
                textView?.text = ""
            }
        }
    }

    fun setUseItem(btn: Button){
        btn.setOnClickListener {
            if(weapons[selected]!=null){

                //funkcja używania np. weapons[selected].attack()

                weapons[selected]!!.endurance -= 1
                if(weapons[selected]!!.endurance == 0){
                    removeWeapon(selected)
                    setBtnImage(selected, R.drawable.default_image)
                    setWeaponImageInImageView(selected)
                }
                setWeaponImageInImageView(selected)
            }else{

                //idk co tam wypisać hoe

            }
        }
    }
}
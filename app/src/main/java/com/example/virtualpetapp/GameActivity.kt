package com.example.virtualpetapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.ColorMatrixColorFilter
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.example.virtualpetapp.model.*
import com.example.virtualpetapp.model.sim.Inventory
import com.example.virtualpetapp.model.sim.Room
import com.example.virtualpetapp.model.SoundPlayer
import kotlinx.coroutines.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Runnable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.math.abs

class GameActivity : AppCompatActivity() {
    companion object {
        var pet: Pet = Pet("", 0, PetClass.CAT, 0, 0f, 0f, 0f, 0f, 0f, LocalDate.now())
        lateinit var inventory: Inventory
        const val SHARED_PREF: String = "SharedPreferences"
        const val SHARED_PREF_PET_NAME: String = "petname"
        const val SHARED_PREF_DEFAULT: String = "default"
        const val SHARED_PREF_LOGOUT_TIME: String = "logoutTime"
        const val FILE_PET: String = "progress"
        const val FILE_INVENTORY: String = "inventory"
    }

    private lateinit var roomList: ArrayList<Room>
    private lateinit var currentRoom: Room
    private lateinit var soundPlayer: SoundPlayer
    private lateinit var sharedPref: SharedPreferences
    private var roomPointer: Int = 0
    private var isPlaying = true
    private var isCatAnimated: Boolean = true
    private var isLitterBoxFull = false
    private var isCatSleeping = false
    private var isStatisticsOnScreen = false

    private val negative = floatArrayOf(
        -1.0f, .0f, .0f, .0f, 255.0f,
        .0f, -1.0f, .0f, .0f, 255.0f,
        .0f, .0f, -1.0f, .0f, 255.0f,
        .0f, .0f, .0f, 1.0f, .0f
    )

    private fun toNegative(d: Drawable): Drawable {
        d.colorFilter = ColorMatrixColorFilter(negative)
        return d
    }

    private fun isLightModeOn(): Boolean {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentMode == Configuration.UI_MODE_NIGHT_NO
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.textViewGameMoney).text = "Money: ${pet.money}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        sharedPref =
            getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        soundPlayer = SoundPlayer(applicationContext)
        val gameMoneyText = findViewById<TextView>(R.id.textViewGameMoney)
        val gameDaysText = findViewById<TextView>(R.id.textViewGameDays)
        val gamePetNameText = findViewById<TextView>(R.id.textViewPetName)
        val gamePetTypeText = findViewById<TextView>(R.id.textViewPetType)
        isStatisticsOnScreen = false
        //check if pet was initialised if not show pet initialisation screen
        if (sharedPref.getString(
                SHARED_PREF_PET_NAME,
                SHARED_PREF_DEFAULT
            ) == SHARED_PREF_DEFAULT
        ) {
            showPetInit()
        } else {//initialise containers on the screen with pet data
            readFromFile()
            gameMoneyText.text = "Money: ${pet.money}"
            gameDaysText.text =
                "Days: " + (LocalDate.now().toEpochDay() - pet.dateCreated.toEpochDay())
            gamePetNameText.text = "Name: " + pet.name
            gamePetTypeText.text = pet.classification.toString()
            if (sharedPref.getLong(SHARED_PREF_LOGOUT_TIME, 0) != 0L) {
                val timeElapsedFromLastLogin =
                    (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - sharedPref.getLong(
                        SHARED_PREF_LOGOUT_TIME,
                        0
                    )).toInt()
                pet.hunger -= 0.01f * (timeElapsedFromLastLogin / 600)
                if (pet.hunger < 0) pet.hunger = 0f
                pet.bladder -= 0.01f * (timeElapsedFromLastLogin / 300)
                if (pet.bladder < 0) pet.bladder = 0f
                pet.hygiene -= 0.01f * (timeElapsedFromLastLogin / 700)
                if (pet.hygiene < 0) pet.hygiene = 0f
                pet.energy += 0.01f * (timeElapsedFromLastLogin / 200)
                if (pet.energy > 1f) pet.energy = 1f
            }
        }

        val buttonShop = findViewById<Button>(R.id.buttonGameShop)
        buttonShop.setOnClickListener {
            soundPlayer.playButtonSound()
            shop()
        }
        val imageView = findViewById<ImageView>(R.id.imageViewRoom)
        val buttonStatistics = findViewById<Button>(R.id.buttonGameStatistics)
        buttonStatistics.setOnClickListener {
            soundPlayer.playButtonSound()
            isStatisticsOnScreen = !isStatisticsOnScreen
            statistics()
        }

        roomList = arrayListOf(Room("Bedroom"), Room("Kitchen"), Room("Bathroom"), Room("Hall"))
        currentRoom = roomList[roomPointer]
        changeRoomName(currentRoom.name)
        changeRoomButtons(roomList[roomPointer])
        val detector =
            GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
                val minDistance = 120
                val minVelocity = 200
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent?,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    val x1 = e1!!.x
                    val x2 = e2!!.x
                    val cat = findViewById<ImageView>(R.id.cat)
                    if (isCatAnimated && imageView.x == 0.0f && !isCatSleeping) {
                        if (x1 - x2 > minDistance && abs(velocityX) > minVelocity) {
                            if (roomPointer == roomList.size - 1) roomPointer = 0
                            else roomPointer += 1
                            //move view 1000f to the left and make it transparent, then when it ends
                            cat.animate().alpha(0f).setDuration(100).start()
                            imageView.animate().translationX(imageView.x - 1000f).alpha(0f)
                                .setDuration(400).withEndAction(
                                    //change room name and image, move view 2000f to the right, then when it ends
                                    Runnable {
                                        changeRoomName(roomList[roomPointer].name)
                                        changeRoomButtons(roomList[roomPointer])
                                        imageView.animate().x(imageView.x + 2000f).setDuration(10)
                                            .withEndAction(
                                                //move view back to the original position
                                                Runnable {
                                                    imageView.animate()
                                                        .translationX(imageView.x - 1000f)
                                                        .alpha(1f)
                                                        .setDuration(400).start()
                                                    cat.animate().alpha(1f).start()
                                                }).start()
                                    }).start()
                        } else if (x2 - x1 > minDistance && abs(velocityX) > minVelocity) {
                            if (roomPointer == 0) roomPointer = roomList.size - 1
                            else roomPointer -= 1
                            //move view 1000f to the right and make it transparent, then when it ends
                            cat.animate().alpha(0f).start()
                            imageView.animate().translationX(imageView.x + 1000f).alpha(0f)
                                .setDuration(400).withEndAction(
                                    //change room name and image, move view 2000f to the left, then when it ends
                                    Runnable {
                                        changeRoomName(roomList[roomPointer].name)
                                        changeRoomButtons(roomList[roomPointer])
                                        imageView.animate().x(imageView.x - 2000f).setDuration(10)
                                            .withEndAction(
                                                //move view back to the original position
                                                Runnable {
                                                    imageView.animate()
                                                        .translationX(imageView.x + 1000f)
                                                        .alpha(1f)
                                                        .setDuration(400).start()
                                                    cat.animate().alpha(1f).start()
                                                }).start()
                                    }).start()
                        }
                    }
                    return true
                }
            })
        imageView.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
            return@setOnTouchListener true
        }
        isPlaying = true
        animateCat()
        updateStatistics()
    }

    override fun onBackPressed() {
        writeToFile()
        isPlaying = false
        finish()
        super.onBackPressed()
    }

    override fun onStop() {
        writeToFile()
        isPlaying = false
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        writeToFile()
        isPlaying = false
        super.onDestroy()
    }

    private fun writeToFile() {
        var fileOutput = applicationContext.openFileOutput(FILE_PET, Context.MODE_PRIVATE)
        var objectOutputStream = ObjectOutputStream(fileOutput)
        objectOutputStream.writeObject(pet)
        fileOutput = applicationContext.openFileOutput(FILE_INVENTORY, Context.MODE_PRIVATE)
        objectOutputStream = ObjectOutputStream(fileOutput)
        objectOutputStream.writeObject(inventory)
        objectOutputStream.close()
        fileOutput.close()
        sharedPref.edit().putLong(
            SHARED_PREF_LOGOUT_TIME, LocalDateTime.now().toEpochSecond(
                ZoneOffset.UTC
            )
        ).commit()
    }

    private fun readFromFile() {
        var fileInput = applicationContext.openFileInput(FILE_PET)
        var objectInputStream = ObjectInputStream(fileInput)
        pet = objectInputStream.readObject() as Pet
        fileInput = applicationContext.openFileInput(FILE_INVENTORY)
        objectInputStream = ObjectInputStream(fileInput)
        inventory = objectInputStream.readObject() as Inventory
        fileInput.close()
        objectInputStream.close()
    }

    private fun animateCat() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            while (isCatAnimated) {
                val catImage = findViewById<ImageView>(R.id.cat)
                if (isCatSleeping) break
                catImage.setImageResource(R.drawable.cat_01)
                delay(3000)
                if (isCatSleeping) break
                catImage.setImageResource(R.drawable.cat_02)
                delay(200)
                if (isCatSleeping) break
                catImage.setImageResource(R.drawable.cat_03)
                delay(200)
                if (isCatSleeping) break
                catImage.setImageResource(R.drawable.cat_04)
                delay(200)
                if (isCatSleeping) break
                catImage.setImageResource(R.drawable.cat_05)
                delay(200)
                if (isCatSleeping) break
                catImage.setImageResource(R.drawable.cat_04)
                delay(200)
                if (isCatSleeping) break
                catImage.setImageResource(R.drawable.cat_03)
                delay(200)
                if (isCatSleeping) break
                catImage.setImageResource(R.drawable.cat_02)
                delay(200)
            }
        }
    }

    private fun changeRoomButtons(r: Room) {
        val button1 = findViewById<ImageButton>(R.id.imageButton)
        val button2 = findViewById<ImageButton>(R.id.imageButton2)
        val button1Text = findViewById<TextView>(R.id.button1Text)
        val button2Text = findViewById<TextView>(R.id.button2Text)
        val imageView = findViewById<ImageView>(R.id.imageViewRoom)
        when (r.name) {
            //Bedroom
            roomList[0].name -> {
                button1.visibility = View.VISIBLE
                if (isLightModeOn()) {
                    button1.setImageDrawable(toNegative(resources.getDrawable(R.drawable.pet_bed)))
                    button2.setImageDrawable(toNegative(resources.getDrawable(R.drawable.ball)))
                } else {
                    button1.setImageResource(R.drawable.pet_bed)
                    button2.setImageResource(R.drawable.ball)
                }
                button1Text.text = "Sleep"
                button2Text.text = "Play"
                imageView.setImageResource(R.drawable.bedroom)
                //play button
                button2.setOnClickListener {
                    soundPlayer.playButtonSound()
                    play()
                }
                button1.setOnClickListener {
                    sleep()
                    soundPlayer.playButtonSound()
                }
            }
            //Kitchen
            roomList[1].name -> {
                button1.visibility = View.GONE
                if (isLightModeOn()) {
                    button2.setImageDrawable((toNegative(resources.getDrawable(R.drawable.food_bowl))))
                } else {
                    button2.setImageResource(R.drawable.food_bowl)
                }
                button1Text.text = ""
                button2Text.text = "Feed"
                imageView.setImageResource(R.drawable.kitchen)
                button2.setOnClickListener {
                    feed()
                    soundPlayer.playButtonSound()
                }
            }
            //Bathroom
            roomList[2].name -> {
                button1.visibility = View.VISIBLE
                if (isLightModeOn()) {
                    button1.setImageDrawable(toNegative(resources.getDrawable(R.drawable.shampoo)))
                    button2.setImageDrawable(toNegative(resources.getDrawable(R.drawable.litter_box)))
                } else {
                    button1.setImageResource(R.drawable.shampoo)
                    button2.setImageResource(R.drawable.litter_box)
                }
                button1Text.text = "Wash: ${inventory.cosmeticItem1}"
                button2Text.text = "Clean Litter Box: ${inventory.cosmeticItem2}"
                if (isLitterBoxFull) imageView.setImageResource(R.drawable.bathroom)
                else imageView.setImageResource(R.drawable.bathroom_clean)
                button1.setOnClickListener {
                    wash()
                    soundPlayer.playButtonSound()
                }
                button2.setOnClickListener {
                    toilet()
                    soundPlayer.playButtonSound()

                }

            }
            //Hall
            roomList[3].name -> {
                button1.visibility = View.GONE
                if (isLightModeOn()) {
                    button2.setImageDrawable(ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.walk,
                        null
                    )
                        ?.let { toNegative(it) })
                } else {
                    button2.setImageResource(R.drawable.walk)
                }
                button1.visibility = View.GONE
                button1Text.text = ""
                button2Text.text = "Walk"
                imageView.setImageResource(R.drawable.hall)
                button2.setOnClickListener {
                    walk()
                    soundPlayer.playButtonSound()
                }
            }
            else -> {}
        }
    }

    private fun changeRoomName(s: String) {
        findViewById<TextView>(R.id.textViewRoomName).text = "Room: $s"
    }

    private fun shop() {
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.shop_layout2, null)
        val item1 = popupView.findViewById<TextView>(R.id.item1)
        val item2 = popupView.findViewById<TextView>(R.id.item2)
        val item3 = popupView.findViewById<TextView>(R.id.item3)
        val item4 = popupView.findViewById<TextView>(R.id.item4)
        val money = popupView.findViewById<TextView>(R.id.textViewShopMoney2)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)
        dialog.show()
        money.text = "Money: ${pet.money}"
        item1.text =
            "Small Food, Quantity: ${inventory.foodItem1} \nFills 25% of Hunger Bar, 1 Coin"
        item2.text =
            "Large Food, Quantity: ${inventory.foodItem2}\nFills 60% of Hunger Bar, 2 Coins"
        item3.text = "Shampoo, Quantity: ${inventory.cosmeticItem1}\nCleans your pet, 3 Coins"
        item4.text = "Litter, Quantity: ${inventory.cosmeticItem2}\nto clean Litterbox, 4 Coins"
        val toast =
            Toast.makeText(applicationContext, "You don't have enought money!", Toast.LENGTH_SHORT)
        popupView.findViewById<Button>(R.id.buttonBuy1).setOnClickListener {
            soundPlayer.playButtonSound()
            if (pet.money >= 1) {
                pet.money -= 1
                inventory.foodItem1 += 1
                item1.text =
                    "Small Food, Quantity: ${inventory.foodItem1}\nFills 25% of Hunger Bar, 1 Coin"
                money.text = "Money: ${pet.money}"
            } else toast.show()
        }
        popupView.findViewById<Button>(R.id.buttonBuy2).setOnClickListener {
            soundPlayer.playButtonSound()
            if (pet.money >= 2) {
                pet.money -= 2
                inventory.foodItem2 += 1
                item2.text =
                    "Large Food, Quantity: ${inventory.foodItem2}\nFills 60% of Hunger Bar, 2 Coins"
                money.text = "Money: ${pet.money}"
            } else toast.show()
        }
        popupView.findViewById<Button>(R.id.buttonBuy3).setOnClickListener {
            soundPlayer.playButtonSound()
            if (pet.money >= 3) {
                pet.money -= 3
                inventory.cosmeticItem1 += 1
                item3.text =
                    "Shampoo, Quantity: ${inventory.cosmeticItem1}\nCleans your pet, 3 Coins"
                money.text = "Money: ${pet.money}"
            } else toast.show()
        }
        popupView.findViewById<Button>(R.id.buttonBuy4).setOnClickListener {
            soundPlayer.playButtonSound()
            if (pet.money >= 4) {
                pet.money -= 4
                inventory.cosmeticItem2 += 1
                item4.text =
                    "Litter, Quantity: ${inventory.cosmeticItem2}\nto clean Litterbox, 4 Coins"
                money.text = "Money: ${pet.money}"
            } else toast.show()
        }
        popupView.findViewById<Button>(R.id.buttonShopClose2).setOnClickListener {
            soundPlayer.playButtonSound()
            dialog.dismiss()
            findViewById<TextView>(R.id.textViewGameMoney).text = "Money: ${pet.money}"
        }
    }

    private fun statistics() {
        val imageView = findViewById<ImageView>(R.id.imageViewRoom)
        imageView.alpha = 0.3f
        val view = layoutInflater.inflate(R.layout.statisticspopup_layout, null)
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        val popupWindow = PopupWindow(view, (point.x / 1.5).toInt(), point.y / 2, true)
        val hunger = pet.hunger.times(100f).toInt()
        val bladder = pet.bladder.times(100f).toInt()
        val hygiene = pet.hygiene.times(100f).toInt()
        val happiness = pet.loneliness.times(100f).toInt()
        val energy = pet.energy.times(100f).toInt()
        view.findViewById<ProgressBar>(R.id.hungerBar).progress = hunger
        view.findViewById<TextView>(R.id.hunger).text = "Hunger:\n $hunger%"
        view.findViewById<ProgressBar>(R.id.bladderBar).progress = bladder
        view.findViewById<TextView>(R.id.bladder).text = "Bladder:\n $bladder%"
        view.findViewById<ProgressBar>(R.id.hygieneBar).progress = hygiene
        view.findViewById<TextView>(R.id.hygiene).text = "Hygiene:\n $hygiene%"
        view.findViewById<ProgressBar>(R.id.happinessBar).progress = happiness
        view.findViewById<TextView>(R.id.hapiness).text = "Happiness:\n $happiness%"
        view.findViewById<ProgressBar>(R.id.energyBar).progress = energy
        view.findViewById<TextView>(R.id.energy).text = "Energy:\n $energy%"
        popupWindow.showAsDropDown(findViewById(R.id.buttonGameStatistics), 0, 0, Gravity.TOP)
        popupWindow.setOnDismissListener {
            imageView.alpha = 1f
        }
    }


    private fun updateStatistics() {
        if (sharedPref.getString(
                SHARED_PREF_PET_NAME,
                SHARED_PREF_DEFAULT
            ) != SHARED_PREF_DEFAULT
        ) {
            updateHunger()
            updateEnergy()
            updateBladder()
            updateHappiness()
            updateHygiene()
        }
    }

    private fun updateHunger() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            while (isPlaying) {
                if (pet.hunger > 0) pet.hunger = pet.hunger.minus(0.01f)
                delay(600000)
            }
        }
    }

    private fun updateEnergy() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            while (isPlaying) {
                if (pet.energy < 0.3f) sleep()
                if (pet.energy > 0 && !isCatSleeping) {
                    pet.energy = pet.energy.minus(0.01f)
                    delay(300000)
                } else if (pet.energy < 1f) {
                    pet.energy = pet.energy.plus(0.01f)
                    delay(15000)
                }
            }
        }
    }

    private fun updateBladder() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            while (isPlaying) {
                if (pet.bladder > 0) pet.bladder = pet.bladder.minus(0.01f)
                if (pet.bladder < 0.5f && !isLitterBoxFull) {
                    isLitterBoxFull = true
                    pet.bladder += 0.5f
                    if (roomList[roomPointer].name == "Bathroom") {
                        findViewById<ImageView>(R.id.imageViewRoom).setImageResource(R.drawable.bathroom)
                    }
                    Toast.makeText(
                        applicationContext,
                        "Your cat used the litter box",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                delay(700000)
            }
        }
    }

    private fun updateHappiness() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            while (isPlaying) {
                var counter = 0
                if (pet.hunger < 0.3f) counter += 1
                if (pet.energy < 0.3f) counter += 1
                if (pet.hygiene < 0.3f) counter += 1
                if (pet.bladder < 0.3f) counter += 1
                if (counter >= 2) {
                    pet.loneliness = pet.loneliness.minus(0.01f)
                }
                if (pet.loneliness == 0.3f || pet.loneliness == 0.15f) {
                    displayDialog()
                }
                if (pet.loneliness == 0f) {
                    displayLoseDialog()
                }
                delay(150000)
            }
        }
    }

    private fun displayDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Your pet happiness dropped to ${(pet.loneliness * 100).toInt()}%, you need to take more care of it, if it reaches 0 your pet will be taken away")
        dialogBuilder.setCancelable(false)
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            soundPlayer.playButtonSound()
            dialog.dismiss()
        }
        dialogBuilder.show()
    }

    private fun displayLoseDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("You do not take care of your pet, so it will be taken away, and you need to start again")
        dialogBuilder.setCancelable(false)
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            soundPlayer.playButtonSound()
            dialog.dismiss()
            sharedPref.edit().putString(
                SHARED_PREF_PET_NAME, "default"
            ).commit()
            finish()
        }
        dialogBuilder.show()
    }

    private fun updateHygiene() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            while (isPlaying) {
                if (pet.hygiene > 0) pet.hygiene = pet.hygiene.minus(0.01f)
                delay(800000)
            }
        }

    }

    //button functions
    private fun play() {
        if (pet.energy >= 0.1f) {
            pet.energy = pet.energy.minus(0.1f)
            val intent = Intent(this, PlayMinigameActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(
                applicationContext,
                "Your cat is to tired to play right now",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun wash() {
        if (inventory.cosmeticItem1 > 0) {
            inventory.cosmeticItem1 -= 1
            val button = findViewById<ImageButton>(R.id.imageButton)
            val button2 = findViewById<ImageButton>(R.id.imageButton2)
            button.isClickable = false
            button.alpha = 0.5f
            button2.isClickable = false
            button2.alpha = 0.5f
            val cat = findViewById<ImageView>(R.id.cat)
            cat.animate().translationX(-300f).translationY(-160f).withEndAction {
                val bathroom = findViewById<ImageView>(R.id.imageViewRoom)
                CoroutineScope(Dispatchers.Main.immediate).launch {
                    for (i in 1..20) {
                        bathroom.setImageResource(R.drawable.bathroom_running_01)
                        delay(200)
                        bathroom.setImageResource(R.drawable.bathroom_running_02)
                        delay(200)
                    }
                    if (isLitterBoxFull) bathroom.setImageResource(R.drawable.bathroom)
                    else bathroom.setImageResource(R.drawable.bathroom_clean)
                    isCatAnimated = true
                    button.isClickable = true
                    button.alpha = 1f
                    button2.isClickable = true
                    button2.alpha = 1f
                    findViewById<TextView>(R.id.button1Text).text =
                        "Wash: ${inventory.cosmeticItem1}"
                    cat.animate().translationX(0f).translationY(0f).withEndAction {
                        pet.hygiene = 1f
                        val toast = Toast.makeText(
                            applicationContext,
                            "Your ${pet.name} is clean now!",
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }.start()
                }
            }.start()
        } else {
            Toast.makeText(
                applicationContext,
                "You don't have enough of this item",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toilet() {
        if (!isLitterBoxFull) {
            Toast.makeText(
                applicationContext,
                "The litter box doesn't need cleaning yet",
                Toast.LENGTH_SHORT
            ).show()
        } else if (inventory.cosmeticItem2 > 0) {
            inventory.cosmeticItem2 -= 1
            isLitterBoxFull = false
        } else {
            Toast.makeText(
                applicationContext,
                "You don't have enough of this item",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun feed() {
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.feedpopup_layout, null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        val buttonCancel = popupView.findViewById<ImageButton>(R.id.feedButtonClose)
        val smallFoodQty = popupView.findViewById<TextView>(R.id.Food2Count)
        val bigFoodQty = popupView.findViewById<TextView>(R.id.Food1Count)
        val buttonSmallFood = popupView.findViewById<ImageButton>(R.id.smallMealButton)
        val buttonBigFood = popupView.findViewById<ImageButton>(R.id.bigMealButton)
        smallFoodQty.text = "Quantity: ${inventory.foodItem1}"
        bigFoodQty.text = "Quantity: ${inventory.foodItem2}"
        dialog.setCancelable(false)
        dialog.show()
        buttonCancel.setOnClickListener {
            soundPlayer.playButtonSound()
            dialog.dismiss()
        }
        buttonSmallFood.setOnClickListener {
            soundPlayer.playButtonSound()
            if (inventory.foodItem1 > 0) {
                pet.hunger += 0.25f
                if (pet.hunger > 1f) pet.hunger = 1f
                inventory.foodItem1 -= 1
                Toast.makeText(
                    applicationContext,
                    "25% was added to your pet food stat",
                    Toast.LENGTH_LONG
                ).show()
                dialog.dismiss()
                soundPlayer.playEatingSound()
            } else {
                Toast.makeText(
                    applicationContext,
                    "You don't have enough of this item",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        buttonBigFood.setOnClickListener {
            soundPlayer.playButtonSound()
            if (inventory.foodItem1 > 0) {
                pet.hunger += 0.60f
                if (pet.hunger > 1f) pet.hunger = 1f
                inventory.foodItem2 -= 1
                Toast.makeText(
                    applicationContext,
                    "60% was added to your pet food stat",
                    Toast.LENGTH_LONG
                ).show()
                dialog.dismiss()
                soundPlayer.playEatingSound()
            } else {
                Toast.makeText(
                    applicationContext,
                    "You don't have enough of this item",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sleep() {
        val button = findViewById<ImageButton>(R.id.imageButton2)
        val room = findViewById<ImageView>(R.id.imageViewRoom)
        val cat = findViewById<ImageView>(R.id.cat)
        if (!isCatSleeping) {
            isCatSleeping = true
            isCatAnimated = false
            button.isClickable = false
            button.alpha = 0.5f
            room.imageAlpha = 100
            cat.setImageResource(R.drawable.cat_sleeping)
        } else {
            if (pet.energy >= 0.1f) {
                isCatSleeping = false
                isCatAnimated = true
                animateCat()
                button.isClickable = true
                room.imageAlpha = 255
                button.alpha = 1f
            } else {
                Toast.makeText(
                    applicationContext,
                    "Your cat is too tired right now",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun walk() {
        if (pet.energy >= 0.1f) {
            pet.energy -= 0.10f
            if (pet.energy < 0) pet.energy = 0f
            pet.hygiene -= 0.15f
            if (pet.hygiene < 0) pet.hygiene = 0f
            pet.loneliness += 0.10f
            if (pet.loneliness > 1f) pet.loneliness = 1f
            val intent = Intent(this, WalkMinigameActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(
                applicationContext,
                "Your cat is to tired to walk right now",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showPetInit() {
        inventory = Inventory(0, 0, 0, 0)
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.pet_chooser_layout, null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        val buttonConfirm = popupView.findViewById<Button>(R.id.buttonPetInitConfirm)
        dialog.setCancelable(false)
        dialog.show()
        val editTextPetName = popupView.findViewById<EditText>(R.id.editTextInitPetName)
        buttonConfirm.setOnClickListener {
            val toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
            val petname = editTextPetName.text
            if (petname.isEmpty()) {
                toast.setText("Choose pet name")
                toast.show()
            } else {
                pet = Pet(
                    petname.toString(),
                    0,
                    PetClass.CAT,
                    0,
                    1f,
                    1f,
                    1f,
                    1f,
                    1f,
                    LocalDate.now()
                )
                sharedPref.edit().putString(SHARED_PREF_PET_NAME, petname.toString()).apply()
                findViewById<TextView>(R.id.textViewGameDays).text = "Day: ${pet.age}"
                findViewById<TextView>(R.id.textViewGameMoney).text = "Money: ${pet.money}"
                findViewById<TextView>(R.id.textViewPetName).text = "Name: ${pet.name}"
                val textPetClass = findViewById<TextView>(R.id.textViewPetType)
                when (pet.classification) {
                    PetClass.CAT -> textPetClass.text = "CAT"
                }
                dialog.dismiss()
            }
        }
    }
}




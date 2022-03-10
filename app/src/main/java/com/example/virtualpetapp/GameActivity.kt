package com.example.virtualpetapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper.loop
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualpetapp.model.*
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*
import org.w3c.dom.Text
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.lang.Runnable
import java.time.LocalDate
import java.util.*

class GameActivity : AppCompatActivity(){
    companion object{
        lateinit var pet: Pet
        lateinit var inventory: Inventory
    }
    private lateinit var roomList: ArrayList<Room>
    private lateinit var currentRoom: Room
    private var roomPointer: Int = 0
    private var isCatAnimated: Boolean = true
    private var isLitterBoxFull = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        val sharedPref = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
        val gameMoneyText = findViewById<TextView>(R.id.textViewGameMoney)
        val gameDaysText = findViewById<TextView>(R.id.textViewGameDays)
        val gamePetNameText = findViewById<TextView>(R.id.textViewPetName)
        val gamePetTypeText = findViewById<TextView>(R.id.textViewPetType)

        //check if pet was initialised if not show pet initialisation screen
        if (sharedPref.getString("petname", "default") == "default") {
            showPetInit()
        }else{//initialise containers on the screen with pet data
            var fileInput = applicationContext.openFileInput("progress")
            var objectInputStream = ObjectInputStream(fileInput)
            pet = objectInputStream.readObject() as Pet
            fileInput = applicationContext.openFileInput("inventory")
            objectInputStream = ObjectInputStream(fileInput)
            inventory = objectInputStream.readObject() as Inventory
            fileInput.close()
            objectInputStream.close()
            gameMoneyText.text = "Money: " + pet.money.toString()
            gameDaysText.text = "Days: " + (LocalDate.now().toEpochDay() - pet.dateCreated.toEpochDay())
            gamePetNameText.text = "Name: " + pet.name
            gamePetTypeText.text = pet.classification.toString()
        }

        val buttonShop = findViewById<Button>(R.id.buttonGameShop)
        buttonShop.setOnClickListener {
            pet.money=100
            shop()
        }

        val buttonStatistics = findViewById<Button>(R.id.buttonGameStatistics)
        buttonStatistics.setOnClickListener {
            statistics()
            animateCat()
        }

        val imageView = findViewById<ImageView>(R.id.imageViewRoom)
        roomList = arrayListOf(Room("Bedroom"),Room("Kitchen"),Room("Bathroom"),Room("Hall"))
        currentRoom = roomList[roomPointer]
        changeRoomName(currentRoom.name)
        changeRoomButtons(roomList[roomPointer])
        val detector = GestureDetector(object : GestureDetector.SimpleOnGestureListener(){
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
                if (isCatAnimated) {
                    if (x1 - x2 > minDistance && Math.abs(velocityX) > minVelocity) {
                        if (roomPointer == roomList.size - 1) roomPointer = 0
                        else roomPointer += 1
                        //move view 1000f to the left and make it transparent, then when it ends
                        cat.animate().alpha(0f).start()
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
                    } else if (x2 - x1 > minDistance && Math.abs(velocityX) > minVelocity) {
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
        imageView.setOnTouchListener { v, event ->
            detector.onTouchEvent(event)
            return@setOnTouchListener true
        }
        animateCat()
        updateStatistics()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        var fileOutput = applicationContext.openFileOutput("progress",Context.MODE_PRIVATE)
        var objectOutputStream = ObjectOutputStream(fileOutput)
        objectOutputStream.writeObject(pet)
        fileOutput = applicationContext.openFileOutput("inventory",Context.MODE_PRIVATE)
        objectOutputStream = ObjectOutputStream(fileOutput)
        objectOutputStream.writeObject(inventory)
        objectOutputStream.close()
        fileOutput.close()
        finish()
    }

    override fun onStop() {
        var fileOutput = applicationContext.openFileOutput("progress",Context.MODE_PRIVATE)
        var objectOutputStream = ObjectOutputStream(fileOutput)
        objectOutputStream.writeObject(pet)
        fileOutput = applicationContext.openFileOutput("inventory",Context.MODE_PRIVATE)
        objectOutputStream = ObjectOutputStream(fileOutput)
        objectOutputStream.writeObject(inventory)
        objectOutputStream.close()
        fileOutput.close()
        super.onStop()
    }
    private fun animateCat(){
        CoroutineScope(Dispatchers.Main.immediate).launch {
            while(isCatAnimated) {
                val catImage = findViewById<ImageView>(R.id.cat)
                catImage.setImageResource(R.drawable.cat_01)
                delay(3000)
                catImage.setImageResource(R.drawable.cat_02)
                delay(200)
                catImage.setImageResource(R.drawable.cat_03)
                delay(200)
                catImage.setImageResource(R.drawable.cat_04)
                delay(200)
                catImage.setImageResource(R.drawable.cat_05)
                delay(200)
                catImage.setImageResource(R.drawable.cat_04)
                delay(200)
                catImage.setImageResource(R.drawable.cat_03)
                delay(200)
                catImage.setImageResource(R.drawable.cat_02)
                delay(200)
            }
        }
    }
    private fun changeRoomButtons(r:Room){
        val button1 = findViewById<ImageButton>(R.id.imageButton)
        val button2 = findViewById<ImageButton>(R.id.imageButton2)
        val button1Text = findViewById<TextView>(R.id.button1Text)
        val button2Text = findViewById<TextView>(R.id.button2Text)
        val imageView = findViewById<ImageView>(R.id.imageViewRoom)
        when(r.name){
            //Bedroom
            roomList[0].name -> {
                button1.visibility = View.VISIBLE
                button1.setImageResource(R.drawable.pet_bed)
                button1Text.text = "Sleep"
                button2.setImageResource(R.drawable.ball)
                button2Text.text = "Play"
                imageView.setImageResource(R.drawable.bedroom)
                //play button
                button2.setOnClickListener {
                    play()
                }
                button1.setOnClickListener {
                    sleep()
                }
            }
            //Kitchen
            roomList[1].name -> {
                button2.setImageResource(R.drawable.food_bowl)
                button2Text.text = "Feed"
                button1.visibility = View.GONE
                button1Text.text = ""
                imageView.setImageResource(R.drawable.kitchen)
                button2.setOnClickListener {
                    feed()
                }
            }
            //Bathroom
            roomList[2].name -> {
                button1.visibility = View.VISIBLE
                button1.setImageResource(R.drawable.shampoo)
                button1Text.text = "Wash: ${inventory.cosmeticItem1}"
                button2.setImageResource(R.drawable.litter_box)
                button2Text.text = "Clean Litterbox: ${inventory.cosmeticItem2}"
                imageView.setImageResource(R.drawable.bathroom)
                button1.setOnClickListener {
                    wash()
                }
                button2.setOnClickListener {
                    toilet()

                }

            }
            //Hall
            roomList[3].name -> {
                button2.setImageResource(R.drawable.walk)
                button1.visibility = View.GONE
                button1Text.text = ""
                button2Text.text = "Walk"
                imageView.setImageResource(R.drawable.hall)
                button2.setOnClickListener {
                    walk()
                }
            }
            else -> {}
        }
    }
    private fun changeRoomName(s: String){
        findViewById<TextView>(R.id.textViewRoomName).text = "Room: " + s
    }
    private fun shop(){
        val sharedPref = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
        sharedPref.edit().putInt("money",pet.money).apply()
        sharedPref.edit().putInt("item1",inventory.foodItem1).apply()
        sharedPref.edit().putInt("item2",inventory.foodItem2).apply()
        sharedPref.edit().putInt("item3",inventory.cosmeticItem1).apply()
        sharedPref.edit().putInt("item4",inventory.cosmeticItem2).apply()
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.shop_layout,null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)
        dialog.show()
        popupView.findViewById<TextView>(R.id.textViewShopMoney).text = "Money: ${pet.money}"
        //create items and populate array lists
        val foodItem = ShopItem("Small Food", 1f, "test description",resources.getDrawable(R.drawable.small_food))
        val foodItem2 = ShopItem("Big Food", 2f,"test description 2",resources.getDrawable(R.drawable.big_food))
        val cosmItem = ShopItem("Cat Shampoo", 3f,"test description 3",resources.getDrawable(R.drawable.shampoo))
        val cosmItem2 = ShopItem("Litter", 4f,"test description 4",resources.getDrawable(R.drawable.litter_box))
        val foodItems: ArrayList<ShopItem> = ArrayList()
        foodItems.add(foodItem)
        foodItems.add(foodItem2)
        val cosmItems: ArrayList<ShopItem> = ArrayList()
        cosmItems.add(cosmItem)
        cosmItems.add(cosmItem2)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.ShopRecyclerView)
        val tabs = dialog.findViewById<TabLayout>(R.id.tabs)
        recyclerView.adapter = RecyclerViewAdapter(foodItems,applicationContext)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if(tabs!=null) {
            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if(tab!=null){
                        if(tab.text.toString().toLowerCase().equals("food"))
                            recyclerView.adapter = RecyclerViewAdapter(foodItems,applicationContext)
                        else
                            recyclerView.adapter = RecyclerViewAdapter(cosmItems,applicationContext)
                    }
                }
            })
        }
        popupView.findViewById<Button>(R.id.buttonShopClose).setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun statistics() {
        val view = layoutInflater.inflate(R.layout.statisticspopup_layout,null)
        val popupWindow = PopupWindow(view,1000,1000,true)
        if(popupWindow.isShowing) {
            popupWindow.dismiss()
        }
        else{
            val hunger = pet.hunger.times(100).toInt()
            val bladder = pet.bladder.times(100).toInt()
            val hygiene = pet.hygiene.times(100).toInt()
            val happiness = pet.loneliness.times(100).toInt()
            val energy = pet.energy.times(100).toInt()
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
            popupWindow.showAsDropDown(findViewById(R.id.buttonGameStatistics),0,0,Gravity.TOP)
        }
    }
    private fun updateStatistics(){
        CoroutineScope(Dispatchers.Main.immediate).launch {
            while(true) {
                if(pet.hunger!=0f){
                delay(86400)
                pet.hunger -= 0.01f
                }
            }
        }
    }
    //button functions
    private fun play(){
        val intent = Intent(this,PlayMinigameActivity::class.java)
        startActivity(intent)
    }
    private fun wash() {
        if (inventory.cosmeticItem1 > 0) {
            inventory.cosmeticItem1 -=1
            val button = findViewById<ImageButton>(R.id.imageButton)
            val button2 = findViewById<ImageButton>(R.id.imageButton2)
            button.isClickable = false
            button.alpha = 0.5f
            button2.isClickable = false
            button2.alpha = 0.5f
            isCatAnimated = false
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
                    bathroom.setImageResource(R.drawable.bathroom)
                    isCatAnimated = true
                    animateCat()
                    button.isClickable = true
                    button.alpha = 1f
                    button2.isClickable = true
                    button2.alpha = 1f
                    findViewById<TextView>(R.id.button1Text).text = "Wash: ${inventory.cosmeticItem1}"
                    cat.animate().translationX(0f).translationY(0f).start()
                }
            }.start()
        }else{
            Toast.makeText(applicationContext, "You don't have enough of this item", Toast.LENGTH_SHORT).show()
        }
    }
    private fun toilet(){
    if(inventory.cosmeticItem2>0) {

    }else{
        Toast.makeText(applicationContext, "You don't have enough of this item", Toast.LENGTH_SHORT).show()
    }
    }
    private fun feed(){
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.feedpopup_layout,null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        val buttonCancel = popupView.findViewById<ImageButton>(R.id.feedButtonClose)
        val smallFoodQty = popupView.findViewById<TextView>(R.id.Food2Count)
        val bigFooodQty = popupView.findViewById<TextView>(R.id.Food1Count)
        smallFoodQty.text = "Quantity: ${inventory.foodItem1}"
        bigFooodQty.text = "Quantity: ${inventory.foodItem2}"
        dialog.setCancelable(false)
        dialog.show()
        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun sleep(){

    }
    private fun walk(){
        val intent = Intent(this,WalkMinigameActivity::class.java)
        startActivity(intent)
    }
    private fun updateDay(){
        findViewById<TextView>(R.id.textViewGameDays).text = "Age: ${pet.age} days"
    }
    private fun changeRoomTitle(title: String){
        findViewById<TextView>(R.id.textViewGameRoomName).text = title
    }
    private fun showPetInit(){
        inventory = Inventory(0,0,0,0)
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.pet_chooser_layout,null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        val buttonConfirm = popupView.findViewById<Button>(R.id.buttonPetInitConfirm)
        dialog.setCancelable(false)
        dialog.show()
        val chipGroup = popupView.findViewById<ChipGroup>(R.id.chipGroupPet)
        val editTextPetName = popupView.findViewById<EditText>(R.id.editTextInitPetName)
        buttonConfirm.setOnClickListener {
            val toast = Toast.makeText(this,"",Toast.LENGTH_SHORT)
            val chip = chipGroup.checkedChipId
            val petname = editTextPetName.text.toString()
            when {
                chip == View.NO_ID -> {
                    toast.setText("Choose pet type")
                    toast.show()
                }
                petname.equals("") -> {
                    toast.setText("Choose pet name")
                    toast.show()
                }
                else -> {
                    val sharedPref = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE)
                    pet = Pet(petname,0,PetClass.CAT,0,1f,1f,1f,1f,1f, LocalDate.now())
                    if(chip==R.id.chipDog) pet.classification = PetClass.DOG
                    sharedPref.edit().putString("petname",petname).apply()
                    findViewById<TextView>(R.id.textViewGameDays).text = "Day: ${pet.age}"
                    findViewById<TextView>(R.id.textViewGameMoney).text = "Money: ${pet.money}"
                    findViewById<TextView>(R.id.textViewPetName).text = "Name: ${pet.name}"
                    val textPetClass = findViewById<TextView>(R.id.textViewPetType)
                    when(pet.classification){
                        PetClass.CAT -> textPetClass.text = "CAT"
                        PetClass.DOG -> textPetClass.text = "DOG"
                    }
                    dialog.dismiss()
                }
            }
        }
    }
}


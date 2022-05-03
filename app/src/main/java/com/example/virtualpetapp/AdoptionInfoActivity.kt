package com.example.virtualpetapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.virtualpetapp.model.SoundPlayer

class AdoptionInfoActivity : AppCompatActivity() {
    lateinit var soundPlayer: SoundPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adoption_info)
        findViewById<Button>(R.id.buttonAdoptionInfoClose).setOnClickListener {
            soundPlayer.playButtonSound()
            finish()
        }
        val buttonList = ArrayList<Button>()
        soundPlayer = SoundPlayer(applicationContext)
        buttonList.add(findViewById(R.id.buttonA))
        buttonList.add(findViewById(R.id.buttonB))
        buttonList.add(findViewById(R.id.buttonC))
        buttonList.add(findViewById(R.id.buttonD))
        for (button in buttonList) {
            button.setOnClickListener {
                createDialog(button.id, button.text.toString())
                soundPlayer.playButtonSound()
            }
        }

    }

    private fun createDialog(id: Int, title: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.adoptionpopup_layout, null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)
        dialog.show()
        popupView.findViewById<TextView>(R.id.textViewTitle).text = title
        val textView = popupView.findViewById<TextView>(R.id.textViewContent)
        var text = ""
        when (id) {
            R.id.buttonA -> {
                text = "Adoption centres are always pleased when someone " +
                        "wants to offer their home to one of their cats, " +
                        "but before you can do that there is some formal " +
                        "requirements you need to meet to adopt a cat.\n\n" +
                        "1. Search for a cat available for adoption\n" +
                        "2. Complete an application form\n" +
                        "3. Visit your cat at the centre\n" +
                        "4. Arrange a home visit with centre crew\n" +
                        "5. Collect your new cat!\n"
                textView.text = text
            }
            R.id.buttonB -> {
                text =
                    "It is completely normal for a cat in new place to behave unusual, " +
                            "they need some time to acclimate to their new home. " +
                            "The unusual behavior may include hiding, destructive behaviors " +
                            "like scratching or biting), refusing to eat or not using the litter box. " +
                            "You can do a few things that will help your new pet adjust:\n\n " +
                            "1. Give them space and provide a safe and comfortable area to hide out\n" +
                            "2. Keep their new environment consistent, do not change anything in the layout of the house\n" +
                            "3. Stay on a regular routine. Feed them at consistent times and play with them regularly\n" +
                            "4. Keep the environment quiet, maybe consider closing the windows if it is loud outside and do not overwhelm them with visitors\n" +
                            "5. Provide them with loads of mental enrichment activities, like interactive toys\n" +
                            "6. If you have other animals in your home, introduce them properly, that means slowly"
                textView.text = text
            }
            R.id.buttonC -> {
                text =
                    "Bringing your new pet home might be very exciting, nonetheless you will need to take some steps before" +
                            "your pet arrives to make sure it will be safe in its surroundings\n\n" +
                            "cat proof your home\n\n" +
                            "cats are very curious beings and they will constantly explore your home" +
                            "to settle in, it is important to block of access to potentially dangerous items for them\n\n" +
                            "make them feel at home\n\n" +
                            "have multiple litter boxes ready and some bowls with wet food and water, to make them acclimate " +
                            "as soon as possible to their new environment"
                textView.text = text
            }
            R.id.buttonD -> {
                text = "1. You are giving an animal a second chance, and save their lives\n\n" +
                        "2. Cost effectiveness - adopting a pet has lower price point than buying from a breeder \n\n" +
                        "3. Shelter pets are often more mature, which eliminates need for basic training \n\n" +
                        "4. Better availability - if you go to your local shelter you will probably get a pet to take home same day, or same week. " +
                        "Which is better compared to 6+ weeks of waiting in a breeders waitlist"
                textView.text = text
            }
        }
        popupView.findViewById<TextView>(R.id.textViewContent).movementMethod =
            ScrollingMovementMethod()
        popupView.findViewById<Button>(R.id.buttonAdoptionInfoPopupClose).setOnClickListener {
            soundPlayer.playButtonSound()
            dialog.dismiss()
        }
    }
}
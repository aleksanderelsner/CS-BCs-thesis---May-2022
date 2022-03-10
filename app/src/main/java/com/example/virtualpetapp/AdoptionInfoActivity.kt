package com.example.virtualpetapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import org.w3c.dom.Text

class AdoptionInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adoption_info)
        findViewById<Button>(R.id.buttonAdoptionInfoClose).setOnClickListener {
            finish()
        }
        val buttonList = ArrayList<Button>()
        buttonList.add(findViewById(R.id.buttonA))
        buttonList.add(findViewById(R.id.buttonB))
        buttonList.add(findViewById(R.id.buttonC))
        buttonList.add(findViewById(R.id.buttonD))
        for(button in buttonList){
            button.setOnClickListener {
                createDialog()
            }
        }

    }
    private fun buttonPressed(){

    }
    private fun setText():String{
    return "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?"
    }
    private fun createDialog(){
        val dialogBuilder = AlertDialog.Builder(this)
        val popupView = layoutInflater.inflate(R.layout.adoptionpopup_layout,null)
        dialogBuilder.setView(popupView)
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)
        dialog.show()
        popupView.findViewById<TextView>(R.id.textViewContent).text = setText()
        popupView.findViewById<TextView>(R.id.textViewContent).movementMethod = ScrollingMovementMethod()
        popupView.findViewById<Button>(R.id.buttonAdoptionInfoPopupClose).setOnClickListener {
            dialog.dismiss()
        }
    }
}
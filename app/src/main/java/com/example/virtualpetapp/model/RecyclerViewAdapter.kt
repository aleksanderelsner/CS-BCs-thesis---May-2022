package com.example.virtualpetapp.model

import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.virtualpetapp.GameActivity
import com.example.virtualpetapp.MainActivity
import com.example.virtualpetapp.R
import org.w3c.dom.Text
import java.io.FileInputStream
import java.io.ObjectInputStream

class RecyclerViewAdapter(private val dataSet: ArrayList<ShopItem>,val context: Context): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    var sharedPref = context.getSharedPreferences("SharedPreferences",Context.MODE_PRIVATE)
    class ViewHolder(view: View,context: Context) : RecyclerView.ViewHolder(view){
        var itemName: TextView = view.findViewById(R.id.textViewShopItemName)
        var itemPrice: TextView = view.findViewById(R.id.price)
        var itemDescription: TextView = view.findViewById(R.id.textViewShopItemDescription)
        var itemImage: ImageView = view.findViewById(R.id.imageViewShopItemImage)
        var itemQuantity: TextView = view.findViewById(R.id.textViewShopItemQuantity)
        var buyButton: Button = view.findViewById<Button>(R.id.buttonBuy)
        init {
            buyButton.setOnClickListener {
                val toast = Toast.makeText(context,"You do not have enough money",Toast.LENGTH_SHORT)
                if(GameActivity.pet.money< itemPrice.text.toString().toInt()){
                    toast.show()
                }else{
                    GameActivity.pet.money-=itemPrice.text.toString().toInt()
                }
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shopitem_layout, parent, false)
        return ViewHolder(view,context)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemDescription.text = dataSet[position].description
        holder.itemName.text = dataSet[position].name
        holder.itemPrice.text = dataSet[position].price.toString()
        holder.itemImage.setImageDrawable(dataSet[position].photo)
        when(position){
            0->{
                holder.itemQuantity.text = "Quantity: ${GameActivity.inventory.foodItem1}"
            }
            1->{
                holder.itemQuantity.text = "Quantity: ${GameActivity.inventory.foodItem2}"
            }
            2->{
                holder.itemQuantity.text = "Quantity: ${GameActivity.inventory.cosmeticItem1}"
            }
            3->{
                holder.itemQuantity.text = "Quantity: ${GameActivity.inventory.cosmeticItem2}"
            }
        }
    }
    override fun getItemCount(): Int = dataSet.size
}
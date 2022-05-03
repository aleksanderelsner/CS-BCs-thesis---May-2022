package com.example.virtualpetapp.model.walk

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.virtualpetapp.R

class Cat(res: Resources) {
    var x: Int = 0
    var y: Int = 0
    var width: Int
    var height: Int
    var cat: Bitmap = BitmapFactory.decodeResource(res, R.drawable.cat_minigame)

    init {
        width = cat.width/2
        height = cat.height/2
        cat = Bitmap.createScaledBitmap(cat, width, height, false)
    }

    fun getRect(): Rect {
        return Rect(x,y+75,x+width,y+height-75)
    }
}
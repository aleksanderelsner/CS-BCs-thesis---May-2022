package com.example.virtualpetapp.model.ball

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.virtualpetapp.R

class Bar(res: Resources) {
    var x: Int = 0
    var y: Int = 0
    var width: Int
    var height: Int
    var bar: Bitmap = BitmapFactory.decodeResource(res, R.drawable.bar)

    init {
        width = bar.width/5
        height = bar.height/5
        bar = Bitmap.createScaledBitmap(bar, width, height, false)
    }

    fun getRect(): Rect {
        return Rect(x,y,x+width,y+height)
    }
}
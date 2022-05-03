package com.example.virtualpetapp.model.walk

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.number.IntegerWidth
import com.example.virtualpetapp.R

class Background(screenX: Int, screenY: Int, res: Resources) {
    var x: Int = 0
    var y: Int = 0
    var background: Bitmap

    init {
        background = BitmapFactory.decodeResource(res, R.drawable.road)
        background = Bitmap.createScaledBitmap(background, screenX, screenY, false)
    }
}
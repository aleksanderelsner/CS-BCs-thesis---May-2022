package com.example.virtualpetapp.model.walk

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.virtualpetapp.R

class Coin(res: Resources) {
    var x: Int = 0
    var y: Int = 0
    var width: Int
    var height: Int
    var coin: Bitmap = BitmapFactory.decodeResource(res, R.drawable.coin)

    init {
        width = coin.width / 2
        height = coin.height / 2
        coin = Bitmap.createScaledBitmap(coin, width, height, false)
    }

    fun getRect(): Rect {
        return Rect(x + 10, y + 10, x + width - 10, y + height - 10)
    }
}
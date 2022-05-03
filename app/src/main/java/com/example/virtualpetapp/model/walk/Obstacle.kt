package com.example.virtualpetapp.model.walk

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.virtualpetapp.R

class Obstacle(res: Resources) {
    var x: Int = 0
    var y: Int = 0
    var width: Int
    var height: Int
    var obstacle: Bitmap

    init {
        obstacle = BitmapFactory.decodeResource(res, R.drawable.obstacle)
        width = obstacle.width / 2
        height = obstacle.height / 2
        obstacle = Bitmap.createScaledBitmap(obstacle, width, height, false)
    }

    fun getRect(): Rect {
        return Rect(x + 10, y + 10, x + width - 10, y + height - 10)
    }
}
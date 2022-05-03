package com.example.virtualpetapp.model.ball

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.example.virtualpetapp.R

class Ball(res: Resources) {
    var x: Int = 0
    var y: Int = 0
    var width: Int
    var height: Int
    var ball: Bitmap = BitmapFactory.decodeResource(res, R.drawable.coin)

    init {
        width = ball.width/3
        height = ball.height/3
        ball = Bitmap.createScaledBitmap(ball, width, height, false)
    }

    fun getRect(): Rect {
        return Rect(x,y,x+width,y+height)
    }
}
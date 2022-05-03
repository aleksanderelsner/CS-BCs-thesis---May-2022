package com.example.virtualpetapp

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.virtualpetapp.view.WalkGameView

class WalkMinigameActivity : AppCompatActivity() {

    private lateinit var walkGameView: WalkGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        walkGameView = WalkGameView(this, point.x, point.y)
        setContentView(walkGameView)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onPause() {
        super.onPause()
        walkGameView.pause()
    }

    override fun onResume() {
        super.onResume()
        walkGameView.resume()
    }

}
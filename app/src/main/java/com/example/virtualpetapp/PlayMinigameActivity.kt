package com.example.virtualpetapp

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.virtualpetapp.view.PlayGameView
import com.example.virtualpetapp.view.WalkGameView

class PlayMinigameActivity : AppCompatActivity() {
    private lateinit var playGameView: PlayGameView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        playGameView = PlayGameView(this, point.x, point.y)
        setContentView(playGameView)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onPause() {
        super.onPause()
        playGameView.pause()
    }

    override fun onResume() {
        super.onResume()
        playGameView.resume()
    }
}
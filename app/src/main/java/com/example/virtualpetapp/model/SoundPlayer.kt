package com.example.virtualpetapp.model

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.provider.MediaStore
import com.example.virtualpetapp.GameActivity
import com.example.virtualpetapp.R

class SoundPlayer(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(GameActivity.SHARED_PREF,Context.MODE_PRIVATE)
    private val mediaPlayerButton: MediaPlayer = MediaPlayer.create(context, R.raw.button)
    private val mediaPlayerEating: MediaPlayer = MediaPlayer.create(context,R.raw.eating)
    private val mediaPlayerCrash: MediaPlayer = MediaPlayer.create(context,R.raw.crash2)
    private val mediaPlayerCoin: MediaPlayer = MediaPlayer.create(context, R.raw.coin)

    fun playButtonSound(){
        if(!sharedPreferences.getBoolean("mute",false)) mediaPlayerButton.start()
    }

    fun playEatingSound(){
        if(!sharedPreferences.getBoolean("mute",false)) mediaPlayerEating.start()
    }

    fun playCrashSound(){
        if(!sharedPreferences.getBoolean("mute",false)) {
            mediaPlayerCrash.start()
        }
    }

    fun playCoinSound(){
        if(!sharedPreferences.getBoolean("mute",false)) mediaPlayerCoin.start()
    }
}
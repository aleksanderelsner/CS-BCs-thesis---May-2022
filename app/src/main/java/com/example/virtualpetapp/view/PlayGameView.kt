package com.example.virtualpetapp.view

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.virtualpetapp.GameActivity
import com.example.virtualpetapp.R
import com.example.virtualpetapp.model.SoundPlayer
import com.example.virtualpetapp.model.ball.Ball
import com.example.virtualpetapp.model.ball.Bar

class PlayGameView(context: Context, screenX: Int, screenY: Int) : SurfaceView(context), Runnable {

    private var isRunning = true;
    lateinit var thread: Thread
    private val paint = Paint()
    private val ball = Ball(resources)
    private val bar = Bar(resources)
    private val screenX = screenX
    private val screenY = screenY
    private var speedX = 20
    private var speedY = 20
    private val audioAttributes =
        AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()
    private val soundPool = SoundPool.Builder().setAudioAttributes(audioAttributes).build()
    private val sharedPref =
        context.getSharedPreferences(GameActivity.SHARED_PREF, Context.MODE_PRIVATE)
    private var sound: Int

    init {
        ball.x = screenX / 2 - ball.width / 2
        ball.y = screenY / 2 - ball.height / 2
        bar.x = screenX / 2 - bar.width / 2
        bar.y = screenY - bar.height * 3
        sound = soundPool.load(context, R.raw.crash, 1)
    }

    override fun run() {
        while (isRunning) {
            update()
            draw()
            Thread.sleep(1)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        while(event?.action==MotionEvent.ACTION_MOVE){
            if(event!=null) bar.x = event.x.toInt()
        }
        return super.onTouchEvent(event)
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        if(event!=null) bar.x = event.x.toInt()-bar.width/2
        return super.onDragEvent(event)
    }

    private fun update() {
        ball.y += speedY
        ball.x += speedX
        //if collides with top
        if ((ball.y < 0)||ball.getRect().intersect(bar.getRect())) {
            speedY = -speedY
            playCrashSound()
        }
        //if collides with bottom
        if ((ball.y > screenY - ball.height)) {
            speedY = -speedY
            playCrashSound()
        }
        //if collides with right
        if ((ball.x > screenX - ball.width)) {
            speedX = -speedX
            playCrashSound()
        }
        //if collides with left
        if ((ball.x < 0)) {
            speedX = -speedX
            playCrashSound()
        }
    }

    private fun playCrashSound() {
        if (!sharedPref.getBoolean("mute", false)) {
            soundPool.play(sound, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            var canvas = holder.lockCanvas()
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(ball.ball, ball.x.toFloat(), ball.y.toFloat(), paint)
            canvas.drawBitmap(bar.bar, bar.x.toFloat(), bar.y.toFloat(), paint)

            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun pause() {
        try {
            isRunning = false
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        isRunning = true;
        thread = Thread(this)
        thread.start()
    }
}
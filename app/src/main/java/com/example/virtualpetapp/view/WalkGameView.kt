package com.example.virtualpetapp.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.virtualpetapp.GameActivity
import com.example.virtualpetapp.R
import com.example.virtualpetapp.model.SoundPlayer
import com.example.virtualpetapp.model.walk.Background
import com.example.virtualpetapp.model.walk.Cat
import com.example.virtualpetapp.model.walk.Coin
import com.example.virtualpetapp.model.walk.Obstacle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class WalkGameView(context: Context, screenX: Int, screenY: Int) : SurfaceView(context), Runnable {
    var isRunning = true
    private lateinit var thread: Thread
    private var soundPlayer: SoundPlayer = SoundPlayer(context)
    var screenY = screenY
    var screenX = screenX
    private var background1: Background = Background(screenX, screenY, resources)
    private var background2: Background = Background(screenX, screenY, resources)
    private var obstacle1: Obstacle = Obstacle(resources)
    private var obstacle2: Obstacle = Obstacle(resources)
    private var obstacle3: Obstacle = Obstacle(resources)
    private var coin: Coin = Coin(resources)
    private var cat: Cat = Cat(resources)
    private var paint = Paint()
    private var textPaint = Paint()
    private var speed: Int = 17;
    private var positions = IntArray(3)
    private var currentPosition = 1
    private var counter = 0
    private var sharedPref =
        context.getSharedPreferences(GameActivity.SHARED_PREF, Context.MODE_PRIVATE)

    init {
        background2.y = screenY
        positions = intArrayOf(
            0 + cat.width / 3,
            screenX / 2 - cat.width / 2,
            screenX - cat.width - cat.width / 3
        )
        textPaint.color = Color.BLACK
        cat.x = positions[currentPosition]
        coin.x = positions[generateRandomPosition()]
        cat.y = screenY - cat.height * 2
        obstacle1.x = positions[generateRandomPosition()]
        obstacle2.x = positions[generateRandomPosition()]
        obstacle2.y -= screenY / 2
        obstacle3.y -= screenY
        obstacle3.x = positions[generateRandomPosition()]
        val detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            val minDistance = 120
            val minVelocity = 200
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val x1 = e1!!.x
                val x2 = e2!!.x
                if (x1 - x2 > minDistance && abs(velocityX) > minVelocity) {
                    if (currentPosition != 0) {
                        currentPosition -= 1
                    }
                } else if (x2 - x1 > minDistance && abs(velocityX) > minVelocity) {
                    if (currentPosition != 2) {
                        currentPosition += 1
                    }
                }
                return true
            }

            override fun onDown(e: MotionEvent?): Boolean {
                if (e != null) {
                    if (!isRunning) {
                        var activity = context as Activity
                        soundPlayer.playButtonSound()
                        activity.finish()
                    }
                }
                return super.onDown(e)
            }
        })
        this.setOnTouchListener { v, event ->
            detector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    override fun run() {
        while (isRunning) {
            update()
            draw()
            Thread.sleep(1)
        }
    }

    fun resume() {
        isRunning = true;
        thread = Thread(this)
        thread.start()
    }

    fun pause() {
        try {
            isRunning = false
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun update() {
        background1.y += speed
        background2.y += speed
        obstacle1.y += speed
        obstacle2.y += speed
        obstacle3.y += speed
        coin.y += speed
        cat.x = positions[currentPosition]
        if (background1.y > background1.background.height) {
            background1.y = -background1.background.height
        }
        if (background2.y > background2.background.height) {
            background2.y = -background2.background.height
        }
        if (obstacle1.y > background1.background.height) {
            obstacle1.x = positions[generateRandomPosition()]
            obstacle1.y = -obstacle1.height
        }
        if (obstacle2.y > background1.background.height) {
            obstacle2.x = positions[generateRandomPosition()]
            obstacle2.y = -obstacle2.height
        }
        if (obstacle3.y > background1.background.height) {
            obstacle3.x = positions[generateRandomPosition()]
            obstacle3.y = -obstacle3.height
        }
        if (coin.y > background1.background.height) {
            CoroutineScope(Dispatchers.Main.immediate).launch {
                delay(5000)
                coin.x = positions[generateRandomPosition()]
                coin.y = -coin.height
            }
        }
        if ((coin.getRect().intersect(obstacle1.getRect()) ||
                    coin.getRect().intersect(obstacle2.getRect()) ||
                    coin.getRect().intersect(obstacle3.getRect()))
        ) {
            coin.y -= coin.height
        }
        if (cat.getRect().intersect(coin.getRect())) {
            coin.y = 5000
            counter += 1
            speed += 3
            soundPlayer.playCoinSound()

        }
        if ((cat.getRect().intersect(obstacle1.getRect()) ||
                    cat.getRect().intersect(obstacle2.getRect()) ||
                    cat.getRect().intersect(obstacle3.getRect()))
        ) {
            draw()
            soundPlayer.playCrashSound()
            isRunning = false
            Thread.sleep(100)
            moveObstacles()
            draw()
            GameActivity.pet.money += counter
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            var canvas = holder.lockCanvas()
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawBitmap(
                background1.background,
                background1.x.toFloat(),
                background1.y.toFloat(),
                paint
            )
            canvas.drawBitmap(
                background2.background,
                background2.x.toFloat(),
                background2.y.toFloat(),
                paint
            )
            canvas.drawBitmap(
                cat.cat,
                cat.x.toFloat(),
                cat.y.toFloat(),
                paint
            )
            canvas.drawBitmap(
                obstacle1.obstacle,
                obstacle1.x.toFloat(),
                obstacle1.y.toFloat(),
                paint
            )
            canvas.drawBitmap(
                obstacle2.obstacle,
                obstacle2.x.toFloat(),
                obstacle2.y.toFloat(),
                paint
            )
            canvas.drawBitmap(
                obstacle3.obstacle,
                obstacle3.x.toFloat(),
                obstacle3.y.toFloat(),
                paint
            )
            canvas.drawBitmap(
                coin.coin,
                coin.x.toFloat(),
                coin.y.toFloat(),
                paint
            )
            if (!isRunning) {
                val hiScore = sharedPref.getInt("hiScore", 0)
                if (counter > hiScore) sharedPref.edit().putInt("hiScore", counter).commit()
                paint.color = Color.LTGRAY
                val rectangle = Rect(100, 100, screenX - 100, screenY - 100)
                canvas.drawRect(Rect(100, 100, screenX - 100, screenY - 300), paint)
                textPaint.textSize = screenY / 20f
                canvas.drawText(
                    "Game Over",
                    rectangle.left + 25f,
                    rectangle.top + textPaint.textSize,
                    textPaint
                )
                canvas.drawText(
                    "Score: ${counter}",
                    rectangle.left + 25f,
                    rectangle.top + textPaint.textSize * 3f,
                    textPaint,
                )
                canvas.drawText(
                    "HiScore: ${hiScore}",
                    rectangle.left + 25f,
                    rectangle.top + textPaint.textSize * 4f,
                    textPaint,
                )
                canvas.drawText(
                    "Touch the",
                    rectangle.left + 150f,
                    rectangle.top + textPaint.textSize * 9f,
                    textPaint
                )
                canvas.drawText(
                    "screen to", rectangle.left + 150f,
                    rectangle.top + textPaint.textSize * 10f,
                    textPaint
                )
                canvas.drawText(
                    "resume", rectangle.left + 150f,
                    rectangle.top + textPaint.textSize * 11f,
                    textPaint
                )
            } else {
                textPaint.textSize = screenY / 30f
                canvas.drawText("Score: ${counter}", 50f, 130f, textPaint)
            }
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun moveObstacles() {
        coin.y = -1000
        obstacle1.y = -1000
        obstacle2.y = -1000
        obstacle3.y = -1000
    }

    private fun generateRandomPosition(): Int {
        return Random.nextInt(0, 3)
    }
}
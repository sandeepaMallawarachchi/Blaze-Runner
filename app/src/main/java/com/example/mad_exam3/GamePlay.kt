package com.example.mad_exam3

import android.content.Intent
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GamePlay : AppCompatActivity(), GameTask {

    private lateinit var characterImageView: ImageView
    private lateinit var frameLayout: FrameLayout
    private val runningImages = intArrayOf(
        R.drawable.run1,
        R.drawable.run2,
        R.drawable.run3,
        R.drawable.run4,
        R.drawable.run5,
        R.drawable.run6,
        R.drawable.run7,
        R.drawable.run8,
    )
    private val jumpingImages = intArrayOf(
        R.drawable.jump4,
        R.drawable.jump5,
        R.drawable.jump6,
        R.drawable.jump7,
        R.drawable.jump8,
        R.drawable.jump9,
        R.drawable.jump10,
        R.drawable.jump11,
        R.drawable.jump12,
    )
    private var isRunning = true
    private var isJumping = false
    private var currentIndex = 0
    private val handler = Handler()

    private lateinit var jumpSoundPlayer: MediaPlayer
    private lateinit var deadSoundPlayer: MediaPlayer
    private lateinit var bgMusicPlayer: MediaPlayer

    private var score = 0

    private lateinit var viewModel: GamePlayViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_play)

        characterImageView = findViewById(R.id.run)
        frameLayout = findViewById(R.id.flameContainer)

        jumpSoundPlayer = MediaPlayer.create(this, R.raw.jump)
        deadSoundPlayer = MediaPlayer.create(this, R.raw.dead)
        bgMusicPlayer = MediaPlayer.create(this, R.raw.bg_music)
        bgMusicPlayer.isLooping = true
        bgMusicPlayer.start()

        viewModel = ViewModelProvider(this).get(GamePlayViewModel::class.java)

        startAnimation()
        setupTouchListener()
        createFlames()
        startGameProgression()
    }

    private var flameSpeed = FLAME_ANIMATION_DELAY

    private fun animateFlame(flame: ImageView) {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val params = flame.layoutParams as FrameLayout.LayoutParams
                params.leftMargin -= 50
                flame.layoutParams = params
                handler.postDelayed(this, flameSpeed.toLong())
            }
        }, flameSpeed.toLong())
    }

    private fun createFlames() {
        for (i in 0 until 100) {
            val flame = ImageView(this)
            flame.setImageResource(R.drawable.flame)
            val layoutParams = FrameLayout.LayoutParams(300, 300)
            layoutParams.topMargin = 520
            layoutParams.leftMargin = 2000 + i * 1500
            flame.layoutParams = layoutParams
            frameLayout.addView(flame)
            animateFlame(flame)
        }
    }

    private fun increaseFlameSpeed() {
        flameSpeed -= 5
        flameSpeed = maxOf(flameSpeed, MIN_FLAME_SPEED)
    }

    private fun startGameProgression() {
        val progressionHandler = Handler()
        progressionHandler.postDelayed(object : Runnable {
            override fun run() {
                increaseFlameSpeed()
                progressionHandler.postDelayed(this, PROGRESSION_INTERVAL.toLong())
            }
        }, PROGRESSION_INTERVAL.toLong())
    }

    private var isLandedRecently = false
    private var landingCooldownDuration = 800
    private var lastLandingTime = 0L

    private fun checkCollision(): Boolean {
        val currentTime = System.currentTimeMillis()

        if (isLandedRecently && currentTime - lastLandingTime < landingCooldownDuration) {
            return false
        }

        val characterBounds = Rect(
            characterImageView.left,
            characterImageView.top,
            characterImageView.right,
            characterImageView.bottom
        )

        val characterBottom = characterImageView.bottom
        val characterCenterX =
            (characterBounds.left + characterBounds.right) / 2

        for (i in 0 until frameLayout.childCount) {
            val flame = frameLayout.getChildAt(i) as ImageView
            val flameBounds = Rect(
                flame.left,
                flame.top,
                flame.right,
                flame.bottom
            )

            val isOnGround =
                characterBottom >= frameLayout.height - 200

            val flameCenterX =
                (flameBounds.left + flameBounds.right) / 2
            val distance = Math.abs(flameCenterX - characterCenterX)

            val collisionThreshold = flame.width

            if (isRunning && isOnGround && characterBounds.intersect(flameBounds)) {
                if (distance < collisionThreshold) {
                    return true
                }
            }

            if (isJumping && characterBounds.intersect(flameBounds)) {
                val verticalCollisionThreshold = 50
                if (flameBounds.bottom - verticalCollisionThreshold > characterBounds.top) {
                    return false
                } else {
                    if (distance < collisionThreshold) {

                        return true
                    }
                }
            }
        }

        return false
    }

    private fun onPlayerLanded() {
        isLandedRecently = true
        lastLandingTime = System.currentTimeMillis()
        handler.postDelayed({ isLandedRecently = false }, landingCooldownDuration.toLong())
        jumpOverFlame()
    }

    private fun setupTouchListener() {
        findViewById<View>(android.R.id.content).setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (isRunning && !isJumping) {
                        // Start jump animation
                        isRunning = false
                        isJumping = true
                        currentIndex = 0

                        characterImageView.translationY = -250f
                        handler.postDelayed({
                            isRunning = true
                            isJumping = false
                            currentIndex = 0

                            characterImageView.translationY = 0f

                            onPlayerLanded()
                        }, JUMP_DURATION.toLong())

                        jumpSoundPlayer.start()
                    }
                }
            }
            true
        }
    }

    private fun startAnimation() {
        handler.post(object : Runnable {
            override fun run() {
                if (!checkCollision()) {
                    val currentImages =
                        if (isRunning) runningImages else jumpingImages
                    characterImageView.setImageResource(currentImages[currentIndex])
                    currentIndex = (currentIndex + 1) % currentImages.size
                    handler.postDelayed(this, ANIMATION_DELAY.toLong())
                } else {
                    showDeathAnimation(score)
                }
            }
        })
    }

    private fun showDeathAnimation(score: Int) {
        handler.removeCallbacksAndMessages(null)

        deadSoundPlayer.start()

        val deadImages =
            IntArray(10) { index -> resources.getIdentifier("dead${index + 1}", "drawable", packageName) }
        var deadImageIndex = 0
        val updateDeadImage = object : Runnable {
            override fun run() {
                if (deadImageIndex < deadImages.size) {
                    characterImageView.setImageResource(deadImages[deadImageIndex])
                    deadImageIndex++
                    handler.postDelayed(this, DEATH_ANIMATION_DELAY.toLong())
                } else {
                    val intent = Intent(this@GamePlay, MainActivity::class.java)
                    intent.putExtra("score", score)
                    startActivity(intent)
                    finish()
                }
            }
        }
        handler.post(updateDeadImage)
    }

    override fun onDestroy() {
        jumpSoundPlayer.release()
        deadSoundPlayer.release()
        bgMusicPlayer.release()
        super.onDestroy()
    }

    override fun updateScore(score: Int) {
        this.score = score
        findViewById<TextView>(R.id.scoreTextView).text = "Score: $score"
        viewModel.updateScore(score) // Update the score in the ViewModel
    }

    override fun gameOver(score: Int) {
        showGameOverScreen(score)
    }

    override fun jumpOverFlame() {
        score++
        updateScore(score)
    }

    private fun showGameOverScreen(score: Int) {
        val intent = Intent(this@GamePlay, MainActivity::class.java)
        intent.putExtra("score", score)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val ANIMATION_DELAY = 100
        private const val FLAME_ANIMATION_DELAY = 100
        private const val DEATH_ANIMATION_DELAY = 100
        private const val JUMP_DURATION = 700
        private const val MIN_FLAME_SPEED = 20
        private const val PROGRESSION_INTERVAL = 5000
    }
}
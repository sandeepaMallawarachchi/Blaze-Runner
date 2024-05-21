package com.example.mad_exam3

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(){

    private lateinit var startBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startBtn = findViewById(R.id.startBtn)

        startBtn.setOnClickListener {
            startActivity(Intent(this, GamePlay::class.java))
        }

        val score = intent.getIntExtra("score", 0)

        val sharedPreferences = getSharedPreferences("GamePreferences", MODE_PRIVATE)
        var highScore = sharedPreferences.getInt("highScore", 0)

        if (score > highScore) {
            highScore = score
            val editor = sharedPreferences.edit()
            editor.putInt("highScore", highScore)
            editor.apply()
        }

        val scoreTextView: TextView = findViewById(R.id.score)
        scoreTextView.text = "Score: $score"

        val highScoreTextView: TextView = findViewById(R.id.highScoreTextView)
        highScoreTextView.text = "High Score: $highScore"
    }
}
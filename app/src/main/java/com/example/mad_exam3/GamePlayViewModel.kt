package com.example.mad_exam3

import androidx.lifecycle.ViewModel

class GamePlayViewModel : ViewModel() {
    private var score: Int = 0

    fun updateScore(newScore: Int) {
        score = newScore
    }
}

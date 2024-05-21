package com.example.mad_exam3

interface GameTask {
    fun updateScore(score: Int)
    fun gameOver(score: Int)
    fun jumpOverFlame()
}
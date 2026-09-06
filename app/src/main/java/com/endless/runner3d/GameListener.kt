package com.endless.runner3d

/**
 * GameEngine orqali UI qatlamiga (Activity) xabar berish uchun interfeys.
 * Chaqiruvlar OpenGL renderlash oqimidan (thread) keladi, shuning uchun
 * amalga oshiruvchi taraf (MainActivity) buni runOnUiThread ichida ishlatishi kerak.
 */
interface GameListener {
    fun onScoreChanged(score: Int, coins: Int)
    fun onGameOver(finalScore: Int, finalCoins: Int)
}

package com.endless.runner3d

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity(), GameListener {

    private lateinit var gameView: GameView
    private lateinit var scoreText: TextView
    private lateinit var coinText: TextView
    private lateinit var gameOverPanel: View
    private lateinit var finalScoreText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView)
        scoreText = findViewById(R.id.scoreText)
        coinText = findViewById(R.id.coinText)
        gameOverPanel = findViewById(R.id.gameOverPanel)
        finalScoreText = findViewById(R.id.finalScoreText)

        gameView.engine.listener = this

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            gameOverPanel.visibility = View.GONE
            gameView.restartGame()
        }
    }

    override fun onResume() {
        super.onResume()
        gameView.onResume()
    }

    override fun onPause() {
        super.onPause()
        gameView.onPause()
    }

    override fun onScoreChanged(score: Int, coins: Int) {
        runOnUiThread {
            scoreText.text = getString(R.string.score_format, score)
            coinText.text = getString(R.string.coin_format, coins)
        }
    }

    override fun onGameOver(finalScore: Int, finalCoins: Int) {
        runOnUiThread {
            finalScoreText.text = getString(R.string.final_score_format, finalScore, finalCoins)
            gameOverPanel.visibility = View.VISIBLE
        }
    }
}

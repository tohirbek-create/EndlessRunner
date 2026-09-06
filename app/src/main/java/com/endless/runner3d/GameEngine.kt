package com.endless.runner3d

import kotlin.math.abs
import kotlin.random.Random

enum class ObstacleType { BLOCK, LOW, HIGH }

data class Obstacle(val lane: Int, var z: Float, val type: ObstacleType, var passed: Boolean = false)
data class Coin(val lane: Int, var z: Float, var collected: Boolean = false)

/**
 * O'yinning barcha mantiqiy holati va yangilanishi shu yerda.
 * Bu klass OpenGL yoki Android UI haqida hech narsa bilmaydi —
 * faqat sof o'yin mantig'i (lane, sakrash fizikasi, to'qnashuv, ball).
 */
class GameEngine {

    // Yo'lak pozitsiyalari (X o'qida): chap, o'rta, o'ng
    val laneX = floatArrayOf(-2.2f, 0f, 2.2f)

    var currentLane = 1
        private set
    var playerX = 0f
        private set

    var isJumping = false
        private set
    var jumpHeight = 0f
        private set
    private var jumpVelocity = 0f
    private val gravity = 20f
    private val jumpImpulse = 7.2f

    var isDucking = false
        private set
    private var duckTimer = 0f
    private val duckDuration = 0.65f

    var speed = 9f
        private set
    private val baseSpeed = 9f
    private val maxSpeed = 20f
    private var distance = 0f

    var score = 0
        private set
    var coins = 0
        private set
    var gameOver = false
        private set

    val obstacles = mutableListOf<Obstacle>()
    val coinList = mutableListOf<Coin>()

    private var spawnTimer = 1.2f
    private var scoreNotifyTimer = 0f

    var listener: GameListener? = null

    fun moveLeft() {
        if (gameOver) return
        if (currentLane > 0) currentLane--
    }

    fun moveRight() {
        if (gameOver) return
        if (currentLane < laneX.size - 1) currentLane++
    }

    fun jump() {
        if (gameOver) return
        if (!isJumping && !isDucking) {
            isJumping = true
            jumpVelocity = jumpImpulse
        }
    }

    fun duck() {
        if (gameOver) return
        if (!isJumping) {
            isDucking = true
            duckTimer = duckDuration
        }
    }

    fun reset() {
        currentLane = 1
        playerX = 0f
        isJumping = false
        jumpHeight = 0f
        jumpVelocity = 0f
        isDucking = false
        duckTimer = 0f
        speed = baseSpeed
        distance = 0f
        score = 0
        coins = 0
        gameOver = false
        obstacles.clear()
        coinList.clear()
        spawnTimer = 1.2f
        scoreNotifyTimer = 0f
        listener?.onScoreChanged(0, 0)
    }

    fun update(dt: Float) {
        if (gameOver) return
        val clampedDt = if (dt > 0.05f) 0.05f else dt // sakrashlarni barqaror qilish uchun chegara

        // Yo'lak orasida silliq siljish
        playerX += (laneX[currentLane] - playerX) * (10f * clampedDt).coerceAtMost(1f)

        // Sakrash fizikasi
        if (isJumping) {
            jumpHeight += jumpVelocity * clampedDt
            jumpVelocity -= gravity * clampedDt
            if (jumpHeight <= 0f) {
                jumpHeight = 0f
                jumpVelocity = 0f
                isJumping = false
            }
        }

        // Egilish taymeri
        if (isDucking) {
            duckTimer -= clampedDt
            if (duckTimer <= 0f) isDucking = false
        }

        // Tezlik va masofa
        speed = (baseSpeed + distance * 0.012f).coerceAtMost(maxSpeed)
        distance += speed * clampedDt
        score = distance.toInt() * 2

        // To'siq va tangalarni oldinga (kamera tomon) siljitish
        for (o in obstacles) o.z += speed * clampedDt
        for (c in coinList) c.z += speed * clampedDt
        obstacles.removeAll { it.z > 4f }
        coinList.removeAll { it.z > 4f || it.collected }

        // To'qnashuvni tekshirish
        for (o in obstacles) {
            if (!o.passed && o.lane == currentLane && o.z in -0.55f..0.55f) {
                val collided = when (o.type) {
                    ObstacleType.BLOCK -> true
                    ObstacleType.LOW -> !(isJumping && jumpHeight > 0.5f)
                    ObstacleType.HIGH -> !isDucking
                }
                if (collided) {
                    triggerGameOver()
                    return
                } else {
                    o.passed = true
                }
            }
        }

        // Tanga yig'ish
        for (c in coinList) {
            if (!c.collected && c.lane == currentLane && c.z in -0.6f..0.6f) {
                c.collected = true
                coins++
                score += 10
            }
        }

        // Yangi to'siq/tanga to'lqinlarini yaratish
        spawnTimer -= clampedDt
        if (spawnTimer <= 0f) {
            spawnWave()
            val minInterval = 0.55f
            val maxInterval = 1.3f
            val difficultyFactor = (speed - baseSpeed) / (maxSpeed - baseSpeed)
            spawnTimer = maxInterval - (maxInterval - minInterval) * difficultyFactor
        }

        // UI ni juda tez-tez yangilamaslik uchun cheklov (~10 marta/soniya)
        scoreNotifyTimer -= clampedDt
        if (scoreNotifyTimer <= 0f) {
            listener?.onScoreChanged(score, coins)
            scoreNotifyTimer = 0.1f
        }
    }

    private fun spawnWave() {
        val spawnZ = -55f
        if (Random.nextFloat() < 0.62f) {
            // To'siq to'lqini: kamida bitta yo'lak har doim bo'sh qoladi
            val blockCount = if (Random.nextFloat() < 0.7f) 1 else 2
            val lanes = (0..2).shuffled().take(blockCount)
            val type = ObstacleType.entries.toTypedArray().random()
            for (lane in lanes) {
                obstacles.add(Obstacle(lane, spawnZ, type))
            }
        } else {
            // Tanga qatori
            val lane = Random.nextInt(3)
            for (i in 0 until 5) {
                coinList.add(Coin(lane, spawnZ - i * 1.3f))
            }
        }
    }

    private fun triggerGameOver() {
        gameOver = true
        listener?.onGameOver(score, coins)
    }
}

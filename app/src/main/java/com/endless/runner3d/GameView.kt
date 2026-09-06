package com.endless.runner3d

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.abs

/**
 * O'yin sahnasini chizadigan GLSurfaceView. Barmoq bilan surish (swipe)
 * orqali yo'lak almashtirish, sakrash va egilishni boshqaradi.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    val engine = GameEngine()
    private val renderer: GameRenderer

    private var touchStartX = 0f
    private var touchStartY = 0f
    private val swipeThreshold = 70f

    init {
        setEGLContextClientVersion(2)
        renderer = GameRenderer(engine)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val dx = event.x - touchStartX
                val dy = event.y - touchStartY
                if (abs(dx) > abs(dy)) {
                    if (abs(dx) > swipeThreshold) {
                        if (dx > 0) engine.moveRight() else engine.moveLeft()
                    }
                } else {
                    if (abs(dy) > swipeThreshold) {
                        if (dy < 0) engine.jump() else engine.duck()
                    }
                }
            }
        }
        return true
    }

    fun restartGame() {
        engine.reset()
    }
}

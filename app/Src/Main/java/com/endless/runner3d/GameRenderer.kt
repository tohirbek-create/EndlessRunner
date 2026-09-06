package com.endless.runner3d

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Sahnani chizuvchi renderer. Har bir freym: (1) o'yin holatini yangilaydi,
 * (2) kamerani sozlaydi, (3) yer/chiziqlar/o'yinchi/to'siqlar/tangalarni chizadi.
 * Fon oddiy bitta rangli osmon (glClearColor) — telefonni qiynamaydi.
 */
class GameRenderer(private val engine: GameEngine) : GLSurfaceView.Renderer {

    private lateinit var cube: Cube
    private var program = 0

    private var positionHandle = 0
    private var normalHandle = 0
    private var mvpMatrixHandle = 0
    private var modelMatrixHandle = 0
    private var colorHandle = 0
    private var lightDirHandle = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var lastTimeNanos = 0L
    private var coinSpin = 0f

    // Ranglar
    private val skyR = 0.55f; private val skyG = 0.78f; private val skyB = 0.95f
    private val groundColor = floatArrayOf(0.35f, 0.55f, 0.30f, 1f)
    private val stripeColor = floatArrayOf(0.92f, 0.92f, 0.92f, 1f)
    private val bodyColor = floatArrayOf(0.20f, 0.42f, 0.90f, 1f)
    private val headColor = floatArrayOf(0.95f, 0.80f, 0.65f, 1f)
    private val blockColor = floatArrayOf(0.85f, 0.20f, 0.20f, 1f)
    private val lowColor = floatArrayOf(0.95f, 0.55f, 0.15f, 1f)
    private val highColor = floatArrayOf(0.60f, 0.25f, 0.80f, 1f)
    private val coinColor = floatArrayOf(1.0f, 0.85f, 0.20f, 1f)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(skyR, skyG, skyB, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // Eslatma: culling ataylab o'chirilgan — sahna juda sodda (bir nechta kub)
        // bo'lgani uchun ishlash tezligiga ta'siri yo'q, lekin barcha yuzlar
        // har doim ko'rinishini kafolatlaydi.

        cube = Cube()
        program = ShaderUtil.buildProgram()
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        normalHandle = GLES20.glGetAttribLocation(program, "aNormal")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        modelMatrixHandle = GLES20.glGetUniformLocation(program, "uModelMatrix")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        lightDirHandle = GLES20.glGetUniformLocation(program, "uLightDir")

        lastTimeNanos = System.nanoTime()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 55f, aspect, 0.5f, 90f)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        var dt = (now - lastTimeNanos) / 1_000_000_000f
        lastTimeNanos = now
        if (dt > 0.1f) dt = 0.1f

        engine.update(dt)
        coinSpin += dt * 200f

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(program)
        GLES20.glUniform3f(lightDirHandle, 0.4f, 1f, 0.6f)

        // Kamera: o'yinchidan biroz yuqorida va orqada, engil gorizontal kuzatish bilan
        val camFollowX = engine.playerX * 0.25f
        Matrix.setLookAtM(
            viewMatrix, 0,
            camFollowX, 4.2f, 7.0f,
            camFollowX, 1.1f, -6f,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        drawGround()
        drawLaneStripes()
        drawObstacles()
        drawCoins()
        drawPlayer()
    }

    private fun drawCube(px: Float, py: Float, pz: Float, sx: Float, sy: Float, sz: Float, color: FloatArray, rotY: Float = 0f) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, px, py, pz)
        if (rotY != 0f) Matrix.rotateM(modelMatrix, 0, rotY, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, sx, sy, sz)

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)
        GLES20.glUniform4fv(colorHandle, 1, color, 0)
        cube.draw(positionHandle, normalHandle)
    }

    private fun drawGround() {
        // Uzun, tekis, bitta rangli yer — oddiy va yengil
        drawCube(0f, -0.15f, -35f, 9f, 0.3f, 110f, groundColor)
    }

    private fun drawLaneStripes() {
        drawCube(-1.1f, 0.001f, -35f, 0.06f, 0.02f, 110f, stripeColor)
        drawCube(1.1f, 0.001f, -35f, 0.06f, 0.02f, 110f, stripeColor)
    }

    private fun drawObstacles() {
        for (o in engine.obstacles) {
            val x = engine.laneX[o.lane]
            when (o.type) {
                ObstacleType.BLOCK -> drawCube(x, 0.7f, o.z, 1.8f, 1.4f, 0.9f, blockColor)
                ObstacleType.LOW -> drawCube(x, 0.3f, o.z, 1.6f, 0.6f, 0.9f, lowColor)
                ObstacleType.HIGH -> drawCube(x, 1.35f, o.z, 1.6f, 0.5f, 0.9f, highColor)
            }
        }
    }

    private fun drawCoins() {
        for (c in engine.coinList) {
            val x = engine.laneX[c.lane]
            drawCube(x, 0.65f, c.z, 0.32f, 0.32f, 0.32f, coinColor, coinSpin)
        }
    }

    private fun drawPlayer() {
        val x = engine.playerX
        val jump = engine.jumpHeight
        val ducking = engine.isDucking

        val bodyHeight = if (ducking) 0.5f else 0.9f
        val bodyY = jump + bodyHeight / 2f
        drawCube(x, bodyY, 0f, 0.8f, bodyHeight, 0.5f, bodyColor)

        val headY = jump + bodyHeight + 0.22f
        drawCube(x, headY, 0f, 0.42f, 0.42f, 0.42f, headColor)
    }
}

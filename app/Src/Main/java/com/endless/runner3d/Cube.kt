package com.endless.runner3d

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Barcha 3D obyektlar (o'yinchi, to'siqlar, tangalar, yer) uchun
 * bitta qayta ishlatiluvchi past-poligonli (low-poly) kub mesh.
 * Har bir yuz uchun alohida normal vektor bilan (flat shading uchun 24 ta vertex).
 */
class Cube {

    private val vertexBuffer: FloatBuffer
    private val normalBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int

    init {
        // Har bir qator: x, y, z  (birlik kub, markazi (0,0,0), qirralari -0.5..0.5)
        val positions = floatArrayOf(
            // OLD (z+)
            -0.5f, -0.5f, 0.5f,   0.5f, -0.5f, 0.5f,   0.5f, 0.5f, 0.5f,   -0.5f, 0.5f, 0.5f,
            // ORQA (z-)
            0.5f, -0.5f, -0.5f,  -0.5f, -0.5f, -0.5f,  -0.5f, 0.5f, -0.5f,  0.5f, 0.5f, -0.5f,
            // CHAP (x-)
            -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f,  -0.5f, 0.5f, 0.5f,  -0.5f, 0.5f, -0.5f,
            // O'NG (x+)
            0.5f, -0.5f, 0.5f,   0.5f, -0.5f, -0.5f,   0.5f, 0.5f, -0.5f,   0.5f, 0.5f, 0.5f,
            // YUQORI (y+)
            -0.5f, 0.5f, 0.5f,   0.5f, 0.5f, 0.5f,   0.5f, 0.5f, -0.5f,   -0.5f, 0.5f, -0.5f,
            // PASTKI (y-)
            -0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f, -0.5f, 0.5f,  -0.5f, -0.5f, 0.5f
        )

        val normals = floatArrayOf(
            0f, 0f, 1f,  0f, 0f, 1f,  0f, 0f, 1f,  0f, 0f, 1f,
            0f, 0f, -1f,  0f, 0f, -1f,  0f, 0f, -1f,  0f, 0f, -1f,
            -1f, 0f, 0f,  -1f, 0f, 0f,  -1f, 0f, 0f,  -1f, 0f, 0f,
            1f, 0f, 0f,  1f, 0f, 0f,  1f, 0f, 0f,  1f, 0f, 0f,
            0f, 1f, 0f,  0f, 1f, 0f,  0f, 1f, 0f,  0f, 1f, 0f,
            0f, -1f, 0f,  0f, -1f, 0f,  0f, -1f, 0f,  0f, -1f, 0f
        )

        val indices = ShortArray(36)
        for (face in 0 until 6) {
            val base = (face * 4).toShort()
            val off = face * 6
            indices[off] = base
            indices[off + 1] = (base + 1).toShort()
            indices[off + 2] = (base + 2).toShort()
            indices[off + 3] = base
            indices[off + 4] = (base + 2).toShort()
            indices[off + 5] = (base + 3).toShort()
        }
        indexCount = indices.size

        vertexBuffer = ByteBuffer.allocateDirect(positions.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(positions); position(0) }
        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(normals); position(0) }
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder()).asShortBuffer().apply { put(indices); position(0) }
    }

    fun draw(positionHandle: Int, normalHandle: Int) {
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        normalBuffer.position(0)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)
        GLES20.glEnableVertexAttribArray(normalHandle)

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
    }
}

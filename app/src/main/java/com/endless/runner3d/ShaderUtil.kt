package com.endless.runner3d

import android.opengl.GLES20

/**
 * Oddiy vertex/fragment shaderlarni kompilyatsiya qilish va
 * dasturga (program) bog'lash uchun yordamchi funksiyalar.
 * Hech qanday tashqi kutubxona ishlatilmagan — faqat android.opengl.GLES20.
 */
object ShaderUtil {

    const val VERTEX_SHADER = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        attribute vec4 aPosition;
        attribute vec3 aNormal;
        varying vec3 vNormal;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vNormal = mat3(uModelMatrix) * aNormal;
        }
    """

    const val FRAGMENT_SHADER = """
        precision mediump float;
        varying vec3 vNormal;
        uniform vec4 uColor;
        uniform vec3 uLightDir;
        void main() {
            vec3 n = normalize(vNormal);
            float diff = max(dot(n, normalize(uLightDir)), 0.0);
            float light = 0.45 + diff * 0.65;
            gl_FragColor = vec4(uColor.rgb * light, uColor.a);
        }
    """

    fun loadShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        return shader
    }

    fun buildProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }
}

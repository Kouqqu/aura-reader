package com.aura.reader.ui.screens.reader.curl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CurlRenderer : GLSurfaceView.Renderer {

    private val mesh = CurlMesh(subdivisionsX = 50, subdivisionsY = 50)

    private var curlProgram: Int = 0
    private var underPageProgram: Int = 0

    // Curl program uniform & attribute handles
    private var uCurlMVPMatrix: Int = 0
    private var uCurlPos: Int = 0
    private var uCurlDir: Int = 0
    private var uCurlRadius: Int = 0
    private var uCurlAspect: Int = 0
    private var uCurlConeFactor: Int = 0
    private var uTextureFront: Int = 0
    private var uTextureBack: Int = 0
    private var uHasBackTexture: Int = 0
    private var aCurlPosition: Int = 0
    private var aCurlTexCoord: Int = 0

    // Under page program uniform & attribute handles
    private var uUnderMVPMatrix: Int = 0
    private var uUnderCurlPos: Int = 0
    private var uUnderCurlDir: Int = 0
    private var uUnderRadius: Int = 0
    private var uUnderAspect: Int = 0
    private var uTextureUnder: Int = 0
    private var aUnderPosition: Int = 0
    private var aUnderTexCoord: Int = 0

    // Textures
    private var textureFrontId: Int = 0
    private var textureBackId: Int = 0
    private var textureUnderId: Int = 0

    private var pendingFrontBitmap: Bitmap? = null
    private var pendingBackBitmap: Bitmap? = null
    private var pendingUnderBitmap: Bitmap? = null
    private var texturesNeedUpdate: Boolean = false

    // Matrices
    private val mvpMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    // Touch & Curl state
    var curlPosX: Float = 1.0f
    var curlPosY: Float = 1.0f
    var curlDirX: Float = -1.0f
    var curlDirY: Float = 0.0f
    var curlRadius: Float = 0.16f
    var coneFactor: Float = 0.08f
    var isCurling: Boolean = false

    private var screenWidth: Int = 1080
    private var screenHeight: Int = 1920
    private var aspect: Float = 1080f / 1920f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.12f, 0.12f, 0.12f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        // Initialize shader programs
        curlProgram = createProgram(
            CurlShaders.PAGE_CURL_VERTEX_SHADER,
            CurlShaders.PAGE_CURL_FRAGMENT_SHADER
        )
        uCurlMVPMatrix = GLES20.glGetUniformLocation(curlProgram, "u_MVPMatrix")
        uCurlPos = GLES20.glGetUniformLocation(curlProgram, "u_CurlPos")
        uCurlDir = GLES20.glGetUniformLocation(curlProgram, "u_CurlDir")
        uCurlRadius = GLES20.glGetUniformLocation(curlProgram, "u_Radius")
        uCurlAspect = GLES20.glGetUniformLocation(curlProgram, "u_Aspect")
        uCurlConeFactor = GLES20.glGetUniformLocation(curlProgram, "u_ConeFactor")
        uTextureFront = GLES20.glGetUniformLocation(curlProgram, "u_TextureFront")
        uTextureBack = GLES20.glGetUniformLocation(curlProgram, "u_TextureBack")
        uHasBackTexture = GLES20.glGetUniformLocation(curlProgram, "u_HasBackTexture")
        aCurlPosition = GLES20.glGetAttribLocation(curlProgram, "a_Position")
        aCurlTexCoord = GLES20.glGetAttribLocation(curlProgram, "a_TexCoord")

        underPageProgram = createProgram(
            CurlShaders.UNDER_PAGE_VERTEX_SHADER,
            CurlShaders.UNDER_PAGE_FRAGMENT_SHADER
        )
        uUnderMVPMatrix = GLES20.glGetUniformLocation(underPageProgram, "u_MVPMatrix")
        uUnderCurlPos = GLES20.glGetUniformLocation(underPageProgram, "u_CurlPos")
        uUnderCurlDir = GLES20.glGetUniformLocation(underPageProgram, "u_CurlDir")
        uUnderRadius = GLES20.glGetUniformLocation(underPageProgram, "u_Radius")
        uUnderAspect = GLES20.glGetUniformLocation(underPageProgram, "u_Aspect")
        uTextureUnder = GLES20.glGetUniformLocation(underPageProgram, "u_TextureUnder")
        aUnderPosition = GLES20.glGetAttribLocation(underPageProgram, "a_Position")
        aUnderTexCoord = GLES20.glGetAttribLocation(underPageProgram, "a_TexCoord")

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height.coerceAtLeast(1)
        aspect = width.toFloat() / height.toFloat()

        GLES20.glViewport(0, 0, width, height)
        Matrix.orthoM(projMatrix, 0, -1f, 1f, -1f, 1f, 0.1f, 10f)
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        checkAndUpdateTextures()

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (isCurling && textureUnderId != 0) {
            drawUnderPage()
        }

        if (textureFrontId != 0) {
            drawCurledPage()
        }
    }

    private fun drawUnderPage() {
        GLES20.glUseProgram(underPageProgram)

        GLES20.glUniformMatrix4fv(uUnderMVPMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniform2f(uUnderCurlPos, curlPosX, curlPosY)
        GLES20.glUniform2f(uUnderCurlDir, curlDirX, curlDirY)
        GLES20.glUniform1f(uUnderRadius, curlRadius)
        GLES20.glUniform1f(uUnderAspect, aspect)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureUnderId)
        GLES20.glUniform1i(uTextureUnder, 0)

        mesh.quadVertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aUnderPosition)
        GLES20.glVertexAttribPointer(aUnderPosition, 2, GLES20.GL_FLOAT, false, 4 * 4, mesh.quadVertexBuffer)

        mesh.quadVertexBuffer.position(2)
        GLES20.glEnableVertexAttribArray(aUnderTexCoord)
        GLES20.glVertexAttribPointer(aUnderTexCoord, 2, GLES20.GL_FLOAT, false, 4 * 4, mesh.quadVertexBuffer)

        mesh.quadIndexBuffer.position(0)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            mesh.quadIndexCount,
            GLES20.GL_UNSIGNED_SHORT,
            mesh.quadIndexBuffer
        )

        GLES20.glDisableVertexAttribArray(aUnderPosition)
        GLES20.glDisableVertexAttribArray(aUnderTexCoord)
    }

    private fun drawCurledPage() {
        GLES20.glUseProgram(curlProgram)

        GLES20.glUniformMatrix4fv(uCurlMVPMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniform2f(uCurlPos, curlPosX, curlPosY)
        GLES20.glUniform2f(uCurlDir, curlDirX, curlDirY)
        GLES20.glUniform1f(uCurlRadius, curlRadius)
        GLES20.glUniform1f(uCurlAspect, aspect)
        GLES20.glUniform1f(uCurlConeFactor, coneFactor)

        // Bind front texture (unit 0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureFrontId)
        GLES20.glUniform1i(uTextureFront, 0)

        // Bind back texture (unit 1)
        if (textureBackId != 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureBackId)
            GLES20.glUniform1i(uTextureBack, 1)
            GLES20.glUniform1i(uHasBackTexture, 1)
        } else {
            GLES20.glUniform1i(uHasBackTexture, 0)
        }

        mesh.vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aCurlPosition)
        GLES20.glVertexAttribPointer(aCurlPosition, 2, GLES20.GL_FLOAT, false, 4 * 4, mesh.vertexBuffer)

        mesh.vertexBuffer.position(2)
        GLES20.glEnableVertexAttribArray(aCurlTexCoord)
        GLES20.glVertexAttribPointer(aCurlTexCoord, 2, GLES20.GL_FLOAT, false, 4 * 4, mesh.vertexBuffer)

        mesh.indexBuffer.position(0)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            mesh.indexCount,
            GLES20.GL_UNSIGNED_SHORT,
            mesh.indexBuffer
        )

        GLES20.glDisableVertexAttribArray(aCurlPosition)
        GLES20.glDisableVertexAttribArray(aCurlTexCoord)
    }

    fun setPages(front: Bitmap?, under: Bitmap?, back: Bitmap? = null) {
        synchronized(this) {
            pendingFrontBitmap = front
            pendingUnderBitmap = under
            pendingBackBitmap = back
            texturesNeedUpdate = true
        }
    }

    private fun checkAndUpdateTextures() {
        if (!texturesNeedUpdate) return
        var front: Bitmap?
        var under: Bitmap?
        var back: Bitmap?

        synchronized(this) {
            front = pendingFrontBitmap
            under = pendingUnderBitmap
            back = pendingBackBitmap
            texturesNeedUpdate = false
        }

        front?.let { textureFrontId = loadTexture(it, textureFrontId) }
        under?.let { textureUnderId = loadTexture(it, textureUnderId) }
        back?.let { textureBackId = loadTexture(it, textureBackId) }
    }

    private fun loadTexture(bitmap: Bitmap, existingId: Int): Int {
        if (bitmap.isRecycled) return existingId
        val texId = if (existingId != 0) {
            existingId
        } else {
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            textures[0]
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        return texId
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            val log = GLES20.glGetProgramInfoLog(program)
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Could not link program: $log")
        }
        return program
    }

    private fun compileShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            val log = GLES20.glGetShaderInfoLog(shader)
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Could not compile shader $type: $log")
        }
        return shader
    }
}

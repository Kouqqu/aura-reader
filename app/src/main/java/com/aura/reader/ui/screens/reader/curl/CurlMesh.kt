package com.aura.reader.ui.screens.reader.curl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class CurlMesh(
    val subdivisionsX: Int = 48,
    val subdivisionsY: Int = 48
) {
    val vertexBuffer: FloatBuffer
    val indexBuffer: ShortBuffer
    val indexCount: Int

    val quadVertexBuffer: FloatBuffer
    val quadIndexBuffer: ShortBuffer
    val quadIndexCount: Int = 6

    init {
        val numCols = subdivisionsX + 1
        val numRows = subdivisionsY + 1
        val totalVertices = numCols * numRows

        // 4 floats per vertex: position (x, y) + texCoord (u, v)
        val vertexData = FloatArray(totalVertices * 4)
        var vIdx = 0

        for (r in 0 until numRows) {
            val y = r.toFloat() / subdivisionsY.toFloat()
            for (c in 0 until numCols) {
                val x = c.toFloat() / subdivisionsX.toFloat()
                vertexData[vIdx++] = x
                vertexData[vIdx++] = y
                vertexData[vIdx++] = x
                vertexData[vIdx++] = y
            }
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertexData)
                position(0)
            }

        // 2 triangles (6 indices) per grid cell
        indexCount = subdivisionsX * subdivisionsY * 6
        val indexData = ShortArray(indexCount)
        var iIdx = 0

        for (r in 0 until subdivisionsY) {
            for (c in 0 until subdivisionsX) {
                val topLeft = (r * numCols + c).toShort()
                val topRight = (r * numCols + c + 1).toShort()
                val bottomLeft = ((r + 1) * numCols + c).toShort()
                val bottomRight = ((r + 1) * numCols + c + 1).toShort()

                // Triangle 1
                indexData[iIdx++] = topLeft
                indexData[iIdx++] = bottomLeft
                indexData[iIdx++] = topRight

                // Triangle 2
                indexData[iIdx++] = topRight
                indexData[iIdx++] = bottomLeft
                indexData[iIdx++] = bottomRight
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(indexData.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(indexData)
                position(0)
            }

        // Flat quad for under-page and drop shadow
        val quadVertices = floatArrayOf(
            0f, 0f,  0f, 0f,
            1f, 0f,  1f, 0f,
            0f, 1f,  0f, 1f,
            1f, 1f,  1f, 1f
        )
        quadVertexBuffer = ByteBuffer.allocateDirect(quadVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(quadVertices)
                position(0)
            }

        val quadIndices = shortArrayOf(0, 2, 1, 1, 2, 3)
        quadIndexBuffer = ByteBuffer.allocateDirect(quadIndices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(quadIndices)
                position(0)
            }
    }
}

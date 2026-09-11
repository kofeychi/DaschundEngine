package kofeychi.taksa.api.vertex.buffer

import kofeychi.taksa.api.*
import kofeychi.taksa.api.vertex.Format
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class QuadBatch(
    private val maxQuads: Int = 4096,
    private val format: Format = Format.position2UvColor(),
    private val usage: BufferUsage = BufferUsage.STREAM_DRAW,
) : AbstractResource() {
    private val vao = TypesafeGL.genVertexArrays()
    private val vbo = TypesafeGL.genBuffers()
    private val staging: ByteBuffer = MemoryUtil.memAlloc(maxQuads * 6 * format.stride)
    private var vertexCount = 0

    init {
        require(format.stride % 4 == 0) { "QuadBatch's default writer requires a 4-byte-multiple stride." }
        require(maxQuads > 0)
        TypesafeGL.bindVertexArray(vao)
        TypesafeGL.bindBuffer(BufferTarget.ARRAY_BUFFER, vbo)
        TypesafeGL.bufferData(BufferTarget.ARRAY_BUFFER, staging.clear(), usage)
        format.apply()
        TypesafeGL.bindBuffer(BufferTarget.ARRAY_BUFFER, BufferId(0))
        TypesafeGL.bindVertexArray(VertexArrayId(0))
    }

    fun begin() {
        check(!isClosed)
        staging.clear()
        vertexCount = 0
    }

    fun quad(
        x: Float, y: Float, width: Float, height: Float,
        u0: Float = 0f, v0: Float = 0f, u1: Float = 1f, v1: Float = 1f,
        r: Float = 1f, g: Float = 1f, b: Float = 1f, a: Float = 1f,
    ) {
        check(!isClosed)
        require(vertexCount + 6 <= maxQuads * 6) { "QuadBatch capacity exceeded; flush before adding more quads." }
        putVertex(x, y, u0, v0, r, g, b, a)
        putVertex(x + width, y, u1, v0, r, g, b, a)
        putVertex(x + width, y + height, u1, v1, r, g, b, a)
        putVertex(x, y, u0, v0, r, g, b, a)
        putVertex(x + width, y + height, u1, v1, r, g, b, a)
        putVertex(x, y + height, u0, v1, r, g, b, a)
    }

    private fun putVertex(x: Float, y: Float, u: Float, v: Float, r: Float, g: Float, b: Float, a: Float) {
        check(format == Format.position2UvColor()) {
            "QuadBatch's convenience writer expects Format.position2UvColor()."
        }
        staging.putFloat(x).putFloat(y).putFloat(u).putFloat(v)
            .putFloat(r).putFloat(g).putFloat(b).putFloat(a)
        vertexCount++
    }

    fun flush(): Int {
        check(!isClosed)
        if (vertexCount == 0) return 0
        staging.flip()
        TypesafeGL.bindVertexArray(vao)
        TypesafeGL.bindBuffer(BufferTarget.ARRAY_BUFFER, vbo)
        TypesafeGL.bufferData(BufferTarget.ARRAY_BUFFER, staging, usage)
        TypesafeGL.drawArrays(DrawMode.TRIANGLES, 0, vertexCount)
        TypesafeGL.bindBuffer(BufferTarget.ARRAY_BUFFER, BufferId(0))
        TypesafeGL.bindVertexArray(VertexArrayId(0))
        val quads = vertexCount / 6
        begin()
        return quads
    }

    override fun onClose() {
        MemoryUtil.memFree(staging)
        TypesafeGL.deleteBuffers(vbo)
        TypesafeGL.deleteVertexArrays(vao)
    }
}

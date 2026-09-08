package kofeychi.taksa.api.vertex.buffer

import kofeychi.taksa.api.BufferTarget
import kofeychi.taksa.api.DrawMode
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.vertex.Buffer
import kofeychi.taksa.api.vertex.DirectBuffer
import kofeychi.taksa.api.vertex.Format
import kofeychi.taksa.api.vertex.VertexArray
import kofeychi.taksa.api.vertex.builder.Slice
import java.io.Closeable

interface IMesh : Closeable {
    fun upload(slice: Slice)

    fun bind()
    fun unbind()

    fun draw()
}

class Mesh(
    format: Format,
    val drawMode: DrawMode,
) : IMesh {
    private val vao: VertexArray = VertexArray(format)
    private val vbo: Buffer = DirectBuffer(
        BufferTarget.ARRAY_BUFFER
    )
    private var vertices = 0

    override fun upload(slice: Slice) {
        vertices = slice.vertexCount
        vao.bind()
        vbo.bind()
        vbo.upload(slice)
        vao.apply()

        vao.unbind()
        vbo.unbind()
    }

    override fun bind() {
        vao.bind()
        vbo.bind()
    }

    override fun unbind() {
        vao.unbind()
        vbo.unbind()
    }

    override fun draw() {
        TypesafeGL.drawArrays(drawMode,0,vertices)
    }

    override fun close() {
        vao.close()
        vbo.close()
    }

}
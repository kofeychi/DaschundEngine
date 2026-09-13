package kofeychi.taksa.api.vertex.buffer

import kofeychi.taksa.api.BufferTarget
import kofeychi.taksa.api.BufferUsage
import kofeychi.taksa.api.DrawMode
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.util.AbstractResource
import kofeychi.taksa.api.util.closeAll
import kofeychi.taksa.api.vertex.Buffer
import kofeychi.taksa.api.vertex.DirectBuffer
import kofeychi.taksa.api.vertex.Format
import kofeychi.taksa.api.vertex.VertexArray
import kofeychi.taksa.api.vertex.builder.Builder
import kofeychi.taksa.api.vertex.builder.Slice

interface IMesh : AutoCloseable {
    fun upload(slice: Slice)
    fun bind()
    fun unbind()
    fun draw()
}

class Mesh(
    format: Format,
    val drawMode: DrawMode,
    usage: BufferUsage = BufferUsage.STREAM_DRAW,
) : AbstractResource(), IMesh {
    private val vao: VertexArray = VertexArray(format)
    private val vbo: Buffer = DirectBuffer(BufferTarget.ARRAY_BUFFER, usage)
    private var vertices = 0

    override fun upload(slice: Slice) {
        checkOpen("mesh")
        vertices = slice.vertexCount
        vao.bind()
        vbo.bind()
        try {
            vbo.upload(slice)
            vao.apply()
        } finally {
            vao.unbind()
            vbo.unbind()
        }
    }

    /** Convenience for `mesh.upload(builder)` - builds the slice, uploads it, and frees the builder's staging memory regardless of outcome. */
    fun upload(builder: Builder) = builder.build(::upload)

    override fun bind() {
        checkOpen("mesh")
        vao.bind()
        vbo.bind()
    }

    override fun unbind() {
        vao.unbind()
        vbo.unbind()
    }

    override fun draw() {
        checkOpen("mesh")
        TypesafeGL.drawArrays(drawMode, 0, vertices)
    }

    override fun free() = closeAll(vao, vbo)
}

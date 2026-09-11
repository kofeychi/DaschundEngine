package kofeychi.taksa.api.vertex.buffer

import kofeychi.taksa.api.*
import kofeychi.taksa.api.vertex.*
import kofeychi.taksa.api.vertex.builder.Builder
import kofeychi.taksa.api.vertex.builder.Slice

class Mesh(
    format: Format,
    val drawMode: DrawMode = DrawMode.TRIANGLES,
    usage: BufferUsage = BufferUsage.STATIC_DRAW,
) : AbstractResource() {
    private val vao = VertexArray(format)
    private val vbo = DirectBuffer(BufferTarget.ARRAY_BUFFER, usage)
    private var vertices = 0

    val format: Format get() = vao.format

    fun upload(slice: Slice): Mesh {
        check(!isClosed)
        slice.requireOpen()
        require(slice.format == format) { "Slice format does not match mesh format." }
        vao.bind()
        try {
            vbo.bind()
            try {
                // DirectBuffer.upload() restores the ARRAY_BUFFER binding when it returns.
                // Re-bind the VBO before configuring vertex attribute pointers:
                // glVertexAttribPointer captures the ARRAY_BUFFER binding into the VAO.
                vbo.upload(slice)
                vbo.bind()
                vao.apply()
            } finally {
                vbo.unbind()
            }
        } finally {
            vao.unbind()
        }
        vertices = slice.vertexCount
        return this
    }

    fun upload(builder: Builder): Mesh = builder.build { upload(it) }

    fun bind() {
        check(!isClosed)
        vao.bind()
        vbo.bind()
    }

    fun unbind() {
        vbo.unbind()
        vao.unbind()
    }

    fun draw() {
        check(!isClosed)
        require(vertices > 0)
        TypesafeGL.drawArrays(drawMode, 0, vertices)
    }

    override fun onClose() {
        unbind()
        vao.close()
        vbo.close()
    }
}

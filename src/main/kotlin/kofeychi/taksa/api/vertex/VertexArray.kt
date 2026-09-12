package kofeychi.taksa.api.vertex

import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.VertexArrayId
import java.io.Closeable

class VertexArray(
    val format: Format,
) : Closeable {
    private val id: VertexArrayId = TypesafeGL.genVertexArrays()

    fun bind() {
        TypesafeGL.bindVertexArray(id)
    }

    fun unbind() {
        TypesafeGL.bindVertexArray(VertexArrayId(0))
    }

    fun apply() {
        format.apply()
    }

    override fun close() {
        TypesafeGL.deleteVertexArrays(id)
    }

}
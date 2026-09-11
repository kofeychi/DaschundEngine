package kofeychi.taksa.api.vertex

import kofeychi.taksa.api.*

class VertexArray(
    val format: Format,
) : AbstractResource() {
    val id = TypesafeGL.genVertexArrays()

    fun bind() { check(!isClosed); TypesafeGL.bindVertexArray(id) }
    fun unbind() { TypesafeGL.bindVertexArray(VertexArrayId(0)) }

    fun apply() {
        check(!isClosed)
        format.apply()
    }

    override fun onClose() {
        TypesafeGL.deleteVertexArrays(id)
    }
}

package kofeychi.taksa.api.vertex

import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.VertexArrayId
import kofeychi.taksa.api.util.AbstractResource

class VertexArray(val format: Format) : AbstractResource() {
    private val id: VertexArrayId = TypesafeGL.genVertexArrays()

    fun bind() {
        checkOpen("vertex array")
        TypesafeGL.bindVertexArray(id)
    }

    fun unbind() = TypesafeGL.bindVertexArray(VertexArrayId(0))

    fun apply() = format.apply()

    override fun free() = TypesafeGL.deleteVertexArrays(id)
}

package kofeychi.taksa.api.vertex

import kofeychi.taksa.api.BufferId
import kofeychi.taksa.api.BufferTarget
import kofeychi.taksa.api.BufferUsage
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.util.AbstractResource
import kofeychi.taksa.api.vertex.builder.Slice

interface Buffer : AutoCloseable {
    fun upload(slice: Slice)
    fun bind()
    fun unbind()
}

class DirectBuffer(
    val type: BufferTarget,
    val usage: BufferUsage = BufferUsage.STREAM_DRAW,
) : AbstractResource(), Buffer {

    private val id = TypesafeGL.genBuffers()
    private var allocated = 0L

    override fun upload(slice: Slice) {
        checkOpen("buffer")
        require(slice.address != 0L) { "Cannot upload a closed Slice" }

        bind()
        try {
            if (allocated < slice.size.toLong()) {
                TypesafeGL.nBufferData(type, slice.size.toLong(), slice.address, usage)
                allocated = slice.size.toLong()
            } else {
                TypesafeGL.nBufferSubData(type, 0, slice.size.toLong(), slice.address)
            }
        } finally {
            unbind()
        }
    }

    override fun bind() {
        checkOpen("buffer")
        TypesafeGL.bindBuffer(type, id)
    }

    override fun unbind() = TypesafeGL.bindBuffer(type, BufferId(0))

    override fun free() = TypesafeGL.deleteBuffers(id)
}

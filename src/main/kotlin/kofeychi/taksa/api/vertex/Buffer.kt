package kofeychi.taksa.api.vertex

import kofeychi.taksa.api.BufferId
import kofeychi.taksa.api.BufferTarget
import kofeychi.taksa.api.BufferUsage
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.vertex.builder.Slice
import java.io.Closeable

interface Buffer : Closeable {
    fun upload(slice: Slice)

    fun bind()

    fun unbind()
}

class DirectBuffer(
    val type: BufferTarget,
    val usage: BufferUsage = BufferUsage.STREAM_DRAW,
) : Buffer {
    private val id = TypesafeGL.genBuffers()
    private var allocated = 0L

    override fun upload(slice: Slice) {
        if(allocated < slice.size) {
            TypesafeGL.nBufferData(
                type,
                slice.size.toLong(),
                slice.address,
                usage,
            )
        } else {
            TypesafeGL.nBufferSubData(
                type,
                0,
                slice.size.toLong(),
                slice.address,
            )
        }
    }

    override fun bind() {
        TypesafeGL.bindBuffer(type, id)
    }

    override fun unbind() {
        TypesafeGL.bindBuffer(type, BufferId(0))
    }

    override fun close() {
        TypesafeGL.deleteBuffers(id)
    }


}
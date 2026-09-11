package kofeychi.taksa.api.vertex

import kofeychi.taksa.api.*
import kofeychi.taksa.api.vertex.builder.Slice

interface Buffer : CleanableResource {
    fun upload(slice: Slice)
    fun upload(bytes: java.nio.ByteBuffer)
    fun bind()
    fun unbind()
}

class DirectBuffer(
    val type: BufferTarget,
    val usage: BufferUsage = BufferUsage.STREAM_DRAW,
) : AbstractResource(), Buffer {
    val id = TypesafeGL.genBuffers()
    private var allocated = 0L

    override fun upload(slice: Slice) {
        check(!slice.isClosed)
        bind()
        try {
            val size = slice.size.toLong()
            if (size > allocated) {
                TypesafeGL.bufferData(type, size, slice.address, usage)
                allocated = size
            } else {
                TypesafeGL.bufferSubData(type, 0, size, slice.address)
            }
        } finally {
            unbind()
        }
    }

    override fun upload(bytes: java.nio.ByteBuffer) {
        check(!isClosed)
        require(bytes.isDirect)
        bind()
        try {
            TypesafeGL.bufferData(type, bytes, usage)
            allocated = bytes.remaining().toLong()
        } finally {
            unbind()
        }
    }

    override fun bind() { check(!isClosed); TypesafeGL.bindBuffer(type, id) }
    override fun unbind() { TypesafeGL.bindBuffer(type, BufferId(0)) }
    override fun onClose() { TypesafeGL.deleteBuffers(id) }
}

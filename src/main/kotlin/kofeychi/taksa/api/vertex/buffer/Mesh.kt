package kofeychi.taksa.api.vertex.buffer

import kofeychi.taksa.api.vertex.builder.Slice
import java.io.Closeable

interface IMesh : Closeable {
    fun upload(slice: Slice)

    fun bind()
    fun unbind()


}

class Mesh : IMesh {
    override fun upload(slice: Slice) {
        TODO("Not yet implemented")
    }

    override fun bind() {
        TODO("Not yet implemented")
    }

    override fun unbind() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}
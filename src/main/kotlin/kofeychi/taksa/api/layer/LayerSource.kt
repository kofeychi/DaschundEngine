package kofeychi.taksa.api.layer

import kofeychi.taksa.api.vertex.builder.Builder
import java.io.Closeable

class LayerSource : Closeable {

    private val buffers = LinkedHashMap<Layer, Builder>()

    fun getBuffer(layer: Layer): Builder {
        return buffers.getOrPut(layer) { layer.builder() }
    }

    fun endBatch() {
        val active = buffers.keys.toList()
        buffers.clear()
        active.forEach { it.flush() }
    }

    fun end(layer: Layer) {
        buffers.remove(layer)
        layer.flush()
    }

    override fun close() {
        buffers.clear()
    }
}
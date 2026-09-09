package kofeychi.taksa.api.layer

import kofeychi.taksa.api.vertex.builder.Builder
import java.io.Closeable

class LayerSource : Closeable {

    private val buffers = LinkedHashMap<RenderLayer, Builder>()

    fun getBuffer(layer: RenderLayer): Builder {
        return buffers.getOrPut(layer) { layer.buffer() }
    }

    fun endBatch() {
        val active = buffers.keys.toList()
        buffers.clear()
        active.forEach { it.flush() }
    }

    fun end(layer: RenderLayer) {
        buffers.remove(layer)
        layer.flush()
    }

    override fun close() {
        buffers.clear()
    }
}
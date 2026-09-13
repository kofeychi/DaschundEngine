package kofeychi.taksa.api.layer

import kofeychi.taksa.api.util.AbstractResource
import kofeychi.taksa.api.vertex.builder.Builder

/**
 * Tracks which [Layer]s were drawn into during the current frame and flushes them,
 * lowest [Layer.zIndex] first, so overlapping layers composite in the expected paint
 * order. A layer only appears here once something is actually drawn to it, keeping
 * idle layers out of the flush cost entirely.
 */
class LayerSource : AbstractResource() {

    private val buffers = LinkedHashMap<Layer, Builder>()

    fun getBuffer(layer: Layer): Builder {
        checkOpen("layer source")
        return buffers.getOrPut(layer) { layer.builder() }
    }

    /** Flushes every layer touched this frame, in ascending z-order, then forgets them for the next frame. */
    fun endBatch() {
        val active = buffers.keys.sortedBy { it.zIndex }
        buffers.clear()
        active.forEach { it.flush() }
    }

    /** Flushes and forgets a single layer immediately, out of band with [endBatch]. */
    fun end(layer: Layer) {
        buffers.remove(layer)
        layer.flush()
    }

    override fun free() {
        buffers.clear()
    }
}

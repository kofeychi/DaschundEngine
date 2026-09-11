package kofeychi.taksa.api.layer

interface LayerSource : AutoCloseable {
    fun snapshot(): List<Layer>
    override fun close() = Unit
}

class MutableLayerSource : LayerSource {
    private val roots = ArrayList<Layer>()
    private val lock = Any()

    fun add(layer: Layer): MutableLayerSource {
        synchronized(lock) {
            if (!roots.contains(layer)) roots += layer
        }
        return this
    }

    fun remove(layer: Layer): Boolean = synchronized(lock) { roots.remove(layer) }

    override fun snapshot(): List<Layer> = synchronized(lock) {
        roots.sortedWith(compareBy<Layer> { it.zIndex }.thenBy { System.identityHashCode(it) })
    }

    override fun close() {
        val snapshot = synchronized(lock) { roots.toList().also { roots.clear() } }
        snapshot.forEach(Layer::close)
    }
}

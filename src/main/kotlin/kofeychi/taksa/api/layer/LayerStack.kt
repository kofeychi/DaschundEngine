package kofeychi.taksa.api.layer

import kofeychi.taksa.api.FrameClock
import org.joml.Matrix4f

class LayerStack(
    private val source: LayerSource,
    private val viewportProvider: () -> IntRect,
    private val projection: Matrix4f = Matrix4f(),
    private val view: Matrix4f = Matrix4f(),
    private val rootTarget: RenderTarget? = null,
) : AutoCloseable {
    val clock = FrameClock()
    private val batchers = ArrayList<AutoCloseable>()

    fun addBatcher(batcher: AutoCloseable): LayerStack {
        batchers += batcher
        return this
    }

    fun draw() {
        val viewport = viewportProvider()
        val timing = clock.tick()
        val projectionCopy = Matrix4f(projection)
        val viewCopy = Matrix4f(view)
        val ctx = DrawCtx(
            timing = timing,
            viewport = viewport,
            projection = projectionCopy,
            view = viewCopy,
            projectionView = Matrix4f(projectionCopy).mul(viewCopy),
            renderTarget = rootTarget,
            activeBatchers = batchers.toList(),
        )
        rootTarget?.bind()
        try {
            source.snapshot()
                .sortedWith(compareBy<Layer> { it.zIndex }.thenBy { System.identityHashCode(it) })
                .forEach { it.draw(ctx) }
        } finally {
            rootTarget?.unbind()
        }
    }

    override fun close() {
        batchers.asReversed().forEach(AutoCloseable::close)
        batchers.clear()
        source.close()
    }
}

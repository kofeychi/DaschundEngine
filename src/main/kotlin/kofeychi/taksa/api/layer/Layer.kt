package kofeychi.taksa.api.layer

import java.util.concurrent.atomic.AtomicLong

fun interface CompositePass : AutoCloseable {
    fun composite(ctx: DrawCtx, source: Framebuffer, destination: RenderTarget?)
    override fun close() = Unit
}

abstract class Layer : AutoCloseable {
    private val order = sequence.incrementAndGet()
    private val childLock = Any()
    private val mutableChildren = ArrayList<Layer>()

    var parent: Layer? = null
        private set
    var zIndex: Int = 0
    var visible: Boolean = true
    var blendMode: BlendMode = BlendMode.ALPHA
    var clip: IntRect? = null

    /** Optional off-screen target owned by this layer. */
    var target: Framebuffer? = null

    /** Called after an off-screen target has been rendered, to composite it to the parent target. */
    var compositor: CompositePass? = null

    val children: List<Layer>
        get() = synchronized(childLock) { mutableChildren.toList() }

    fun addChild(child: Layer): Layer {
        require(child !== this)
        var p: Layer? = this
        while (p != null) {
            require(p !== child) { "Layer cycle detected." }
            p = p.parent
        }
        child.parent?.removeChild(child)
        child.parent = this
        synchronized(childLock) { mutableChildren += child }
        return child
    }

    fun removeChild(child: Layer): Boolean {
        val removed = synchronized(childLock) { mutableChildren.remove(child) }
        if (removed) child.parent = null
        return removed
    }

    fun sortedChildren(): List<Layer> =
        synchronized(childLock) { mutableChildren.sortedWith(compareBy<Layer> { it.zIndex }.thenBy { it.order }) }

    fun draw(ctx: DrawCtx) {
        if (!visible) return

        val parentTarget = ctx.renderTarget
        val effectiveClip = if (ctx.clip == null) clip else ctx.clip?.let { ctx.clip.intersect(it) }
        val output = target

        if (output != null) {
            output.bind()
            try {
                output.clear()
                val localCtx = ctx.copy(
                    viewport = IntRect(0, 0, output.width, output.height),
                    renderTarget = output,
                    clip = effectiveClip,
                )
                localCtx.applyViewportAndClip()
                blendMode.apply()
                onDraw(localCtx)
                sortedChildren().forEach { it.draw(localCtx) }
                onAfterDraw(localCtx)
            } finally {
                output.unbind()
            }
            compositor?.composite(
                ctx.copy(renderTarget = parentTarget, clip = effectiveClip),
                output,
                parentTarget,
            )
        } else {
            val localCtx = ctx.copy(clip = effectiveClip)
            localCtx.applyViewportAndClip()
            blendMode.apply()
            onDraw(localCtx)
            sortedChildren().forEach { it.draw(localCtx) }
            onAfterDraw(localCtx)
        }
    }

    protected open fun onDraw(ctx: DrawCtx) = Unit
    protected open fun onAfterDraw(ctx: DrawCtx) = Unit

    override fun close() {
        val snapshot = synchronized(childLock) { mutableChildren.toList().also { mutableChildren.clear() } }
        snapshot.forEach { child ->
            child.parent = null
            child.close()
        }
        compositor?.close()
        compositor = null
        target?.close()
        target = null
    }

    companion object {
        private val sequence = AtomicLong()
    }
}

class DrawLayer(private val renderer: (DrawCtx) -> Unit) : Layer() {
    override fun onDraw(ctx: DrawCtx) = renderer(ctx)
}

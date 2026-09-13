package kofeychi.taksa.api.layer

import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.util.AbstractResource
import kofeychi.taksa.api.util.closeAll

/**
 * Owns a set of [Layer]s plus the [LayerSource] batching them, and drives one frame's
 * worth of draw calls in a single [frame] call. Layers can be added/removed dynamically
 * (e.g. spawning a new particle-effect layer at runtime); ordering is always derived
 * from [Layer.zIndex] at flush time, so insertion order never matters.
 *
 * Supports a simple post-processing pipeline: each registered [CompositePass] renders
 * this stack into an offscreen [RenderTarget] first, runs a shader over the result, and
 * only the final pass writes to the default framebuffer - useful for bloom, color
 * grading, blur, CRT effects, etc without touching layer code at all.
 */
class LayerStack : AbstractResource() {

    private val layers = mutableListOf<Layer>()
    val source = LayerSource()

    /** Chained offscreen effects applied after the layer tree renders, in order. Empty means "draw straight to the bound framebuffer". */
    val postProcessing = mutableListOf<CompositePass>()

    fun add(layer: Layer): Layer {
        checkOpen("layer stack")
        layers += layer
        return layer
    }

    fun remove(layer: Layer, closeIt: Boolean = true) {
        layers -= layer
        if (closeIt) layer.close()
    }

    /** Renders one frame: draws every visible layer (parents drawn before their z-ordered children get a chance to composite over them), then runs post-processing. */
    fun frame(width: Int, height: Int, deltaNanos: Long = 0L, fps: Double = 0.0, draw: (Layer, DrawCtx) -> Unit) {
        checkOpen("layer stack")

        fun render() {
            TypesafeGL.viewport(0, 0, width, height)
            for (layer in layers.sortedWith(compareBy({ it.parent != null }, { it.zIndex }))) {
                if (!layer.effectivelyVisible) continue
                draw(layer, DrawCtx(width, height, source, layer, deltaNanos, fps))
            }
            source.endBatch()
        }

        if (postProcessing.isEmpty()) {
            render()
            return
        }

        // Pass 0 always reads the live scene (input = null); render it into an offscreen target first.
        val sceneTarget = RenderTarget.create(width, height, depth = true)
        sceneTarget.use(intArrayOf(0, 0, width, height)) { render() }

        var previous: RenderTarget = sceneTarget
        for ((index, pass) in postProcessing.withIndex()) {
            val isLast = index == postProcessing.lastIndex
            if (isLast) {
                pass.apply(previous)
            } else {
                val target = RenderTarget.create(width, height)
                target.use(intArrayOf(0, 0, width, height)) { pass.apply(previous) }
                previous.close()
                previous = target
            }
        }
        previous.close()
    }

    override fun free() {
        closeAll(layers)
        layers.clear()
        closeAll(postProcessing)
        postProcessing.clear()
        source.close()
    }
}

/** A single offscreen effect stage: reads the previous stage's [input] (the raw scene for the first pass) and writes to whatever framebuffer is currently bound. */
interface CompositePass : AutoCloseable {
    fun apply(input: RenderTarget)
    override fun close() {}
}

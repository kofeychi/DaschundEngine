package kofeychi.taksa.api.layer

import kofeychi.taksa.api.BlendMode
import kofeychi.taksa.api.BufferUsage
import kofeychi.taksa.api.DrawMode
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.shader.Program
import kofeychi.taksa.api.util.AbstractResource
import kofeychi.taksa.api.util.closeAll
import kofeychi.taksa.api.vertex.Format
import kofeychi.taksa.api.vertex.buffer.Mesh
import kofeychi.taksa.api.vertex.builder.Builder
import kofeychi.taksa.api.vertex.builder.DirectBuilder

/** Axis-aligned pixel-space clip rect applied as a GL scissor while this layer draws. */
data class ClipRect(val x: Int, val y: Int, val width: Int, val height: Int)

/**
 * A single batched draw target: everything pushed into its [Builder] during a frame is
 * flushed as one draw call. Layers can be parented for hierarchical grouping (e.g. a
 * "HUD" parent containing "healthbar" + "minimap" children), z-ordered for correct
 * paint order via [LayerSource]/[LayerStack], toggled invisible without losing state,
 * and auto-clipped to a [clip] rect.
 */
class Layer(
    val program: Program,
    val drawMode: DrawMode,
    val format: Format,
    val bufferUsage: BufferUsage = BufferUsage.STREAM_DRAW,
    var zIndex: Int = 0,
    var visible: Boolean = true,
    var blendMode: BlendMode = BlendMode.ALPHA,
    var clip: ClipRect? = null,
    var parent: Layer? = null,
    val builderFactory: () -> Builder = { DirectBuilder(4096, format) },
) : AbstractResource() {

    private var mesh: Mesh? = null
    private var builder: Builder? = null

    /** Effective visibility accounting for ancestor visibility - a hidden parent hides every descendant. */
    val effectivelyVisible: Boolean
        get() = visible && (parent?.effectivelyVisible ?: true)

    fun builder(): Builder {
        checkOpen("layer")
        if (builder == null) builder = builderFactory()
        return builder!!
    }

    fun mesh(): Mesh {
        if (mesh == null) mesh = Mesh(format, drawMode, bufferUsage)
        return mesh!!
    }

    /** Uploads and draws whatever has been batched, then clears the builder for the next frame. No-op if nothing was pushed, invisible, or closed. */
    internal fun flush() {
        val current = builder ?: return
        if (!effectivelyVisible) {
            current.clear()
            builder = null
            return
        }

        try {
            current.view().use { slice -> mesh().upload(slice) }
        } catch (e: IllegalStateException) {
            if (!e.message.orEmpty().contains("no vertices", ignoreCase = true)) throw e
            builder = null
            return
        } finally {
            builder?.clear()
            builder = null
        }

        val activeClip = clip
        if (activeClip != null) TypesafeGL.setScissor(activeClip.x, activeClip.y, activeClip.width, activeClip.height)

        TypesafeGL.setBlendMode(blendMode)
        program.bind()
        mesh().bind()
        mesh().draw()
        mesh().unbind()
        program.unbind()

        if (activeClip != null) TypesafeGL.clearScissor()
    }

    override fun free() {
        closeAll(builder, mesh)
        builder = null
        mesh = null
    }
}

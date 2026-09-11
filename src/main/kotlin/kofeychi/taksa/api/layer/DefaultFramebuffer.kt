package kofeychi.taksa.api.layer

import kofeychi.taksa.api.FramebufferId
import kofeychi.taksa.api.TypesafeGL

/**
 * Adapts the application's window/default framebuffer to RenderTarget.
 * The caller owns the window/context; therefore close() is a no-op.
 */
class DefaultFramebuffer(
    private val size: () -> IntRect,
) : RenderTarget {
    override val width: Int get() = size().width
    override val height: Int get() = size().height

    override fun bind() {
        TypesafeGL.bindFramebuffer(FramebufferId(0))
        val viewport = size()
        TypesafeGL.viewport(viewport.x, viewport.y, viewport.width, viewport.height)
    }

    override fun unbind() = Unit

    override fun clear(r: Float, g: Float, b: Float, a: Float) {
        bind()
        TypesafeGL.clearColor(r, g, b, a)
        TypesafeGL.clear(
            org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT or
                org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT or
                org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT
        )
    }

    override fun close() = Unit
}

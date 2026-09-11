package kofeychi.taksa.api.layer

import kofeychi.taksa.api.FrameTiming
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11

data class IntRect(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun intersect(other: IntRect): IntRect? {
        val nx = maxOf(x, other.x)
        val ny = maxOf(y, other.y)
        val nr = minOf(x + width, other.x + other.width)
        val nb = minOf(y + height, other.y + other.height)
        if (nr <= nx || nb <= ny) return null
        return IntRect(nx, ny, nr - nx, nb - ny)
    }
}

interface RenderTarget : AutoCloseable {
    val width: Int
    val height: Int
    fun bind()
    fun unbind()
    fun clear(r: Float = 0f, g: Float = 0f, b: Float = 0f, a: Float = 0f)
}

data class DrawCtx(
    val timing: FrameTiming,
    val viewport: IntRect,
    val projection: Matrix4f,
    val view: Matrix4f,
    val projectionView: Matrix4f,
    val renderTarget: RenderTarget?,
    val activeBatchers: List<AutoCloseable>,
    val clip: IntRect? = null,
) {
    val deltaNanos get() = timing.deltaNanos
    val fps get() = timing.fps
    val deltaSeconds get() = timing.deltaSeconds

    fun withClip(next: IntRect?): DrawCtx {
        val combined = if (clip == null) next else if (next == null) clip else clip.intersect(next)
        return copy(clip = combined)
    }

    fun applyViewportAndClip() {
        org.lwjgl.opengl.GL11.glViewport(viewport.x, viewport.y, viewport.width, viewport.height)
        val effective = clip ?: viewport
        org.lwjgl.opengl.GL11.glEnable(GL11.GL_SCISSOR_TEST)
        if (effective.width <= 0 || effective.height <= 0) {
            org.lwjgl.opengl.GL11.glScissor(0, 0, 0, 0)
        } else {
            org.lwjgl.opengl.GL11.glScissor(effective.x, effective.y, effective.width, effective.height)
        }
    }
}

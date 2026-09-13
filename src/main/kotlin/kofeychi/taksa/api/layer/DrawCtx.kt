package kofeychi.taksa.api.layer

import kofeychi.taksa.api.vertex.builder.Builder
import org.joml.Matrix4f
import kotlin.math.sqrt

/**
 * Everything a piece of draw code needs for one frame: viewport size, frame timing,
 * projection/view matrices, and the active [LayerSource] to batch into. Passed down
 * through the layer tree instead of being read from globals, so nested/offscreen
 * passes (see [RenderTarget]) can hand child layers a context scoped to their own
 * target size without disturbing the outer frame.
 */
class DrawCtx(
    val width: Int,
    val height: Int,
    val buffers: LayerSource,
    val layer: Layer,
    val deltaNanos: Long = 0L,
    val fps: Double = 0.0,
    val projection: Matrix4f = Matrix4f().ortho2D(0f, width.toFloat(), height.toFloat(), 0f),
    val view: Matrix4f = Matrix4f(),
) {
    init {
        require(width > 0) { "width must be positive." }
        require(height > 0) { "height must be positive." }
    }

    val deltaSeconds: Float get() = (deltaNanos / 1_000_000_000.0).toFloat()

    /** Returns a copy of this context bound to a different [layer] but sharing frame timing/matrices - handy for drawing several layers within the same DrawCtx.() extension. */
    fun withLayer(layer: Layer): DrawCtx = DrawCtx(width, height, buffers, layer, deltaNanos, fps, projection, view)

    fun fill(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        val b = buffers.getBuffer(layer)
        quad(b, left, top, right, bottom, color)
    }

    fun rect(x: Float, y: Float, width: Float, height: Float, color: Int) =
        fill(x, y, x + width, y + height, color)

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int) {
        require(width >= 0f) { "Line width must be >= 0." }
        if (width == 0f) return
        val dx = x2 - x1
        val dy = y2 - y1
        val length = sqrt(dx * dx + dy * dy)
        if (length == 0f) {
            rect(x1 - width * 0.5f, y1 - width * 0.5f, width, width, color)
            return
        }

        val half = width * 0.5f
        val px = -dy / length * half
        val py = dx / length * half

        val b = buffers.getBuffer(layer)
        vertex(b, x1 + px, y1 + py, 0f, color)
        vertex(b, x2 + px, y2 + py, 0f, color)
        vertex(b, x2 - px, y2 - py, 0f, color)
        vertex(b, x1 + px, y1 + py, 0f, color)
        vertex(b, x2 - px, y2 - py, 0f, color)
        vertex(b, x1 - px, y1 - py, 0f, color)
    }

    fun flush() = buffers.endBatch()

    private fun quad(b: Builder, left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        vertex(b, left, top, 0f, color)
        vertex(b, right, top, 0f, color)
        vertex(b, right, bottom, 0f, color)
        vertex(b, left, top, 0f, color)
        vertex(b, right, bottom, 0f, color)
        vertex(b, left, bottom, 0f, color)
    }

    private fun vertex(b: Builder, x: Float, y: Float, z: Float, color: Int) {
        val nx = (x / width.toFloat()) * 2f - 1f
        val ny = 1f - (y / height.toFloat()) * 2f

        b.pushFloat3(nx, ny, z)
        b.putByte((color ushr 16).toByte())
        b.putByte((color ushr 8).toByte())
        b.putByte(color.toByte())
        b.putByte((color ushr 24).toByte())
        b.push()
    }
}

package kofeychi.taksa.api.layer

import kofeychi.taksa.api.FramebufferId
import kofeychi.taksa.api.RenderbufferId
import kofeychi.taksa.api.TextureInternalFormat
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.texture.Texture
import kofeychi.taksa.api.util.AbstractResource
import org.lwjgl.opengl.GL30

/**
 * An offscreen framebuffer: one color-attached [Texture] you can render into and later
 * sample from (post-processing, composited layers, render-to-texture UI, etc).
 *
 * ```
 * val target = RenderTarget.create(1920, 1080, depth = true)
 * target.use {
 *     TypesafeGL.clear(color = true, depth = true)
 *     // ... draw scene ...
 * }
 * someProgram.useUniform("uScene") { set(target.colorTexture) }
 * ```
 */
class RenderTarget private constructor(
    val width: Int,
    val height: Int,
    val colorTexture: Texture,
    private val depthStencilRbo: RenderbufferId?,
) : AbstractResource() {

    private val fbo: FramebufferId = TypesafeGL.genFramebuffers()

    init {
        TypesafeGL.bindFramebuffer(fbo)
        try {
            TypesafeGL.framebufferTexture2D(GL30.GL_COLOR_ATTACHMENT0, colorTexture.configuration.target, colorTexture.id)
            if (depthStencilRbo != null) {
                TypesafeGL.framebufferRenderbuffer(GL30.GL_DEPTH_STENCIL_ATTACHMENT, depthStencilRbo)
            }
            val status = TypesafeGL.checkFramebufferStatus()
            check(status == GL30.GL_FRAMEBUFFER_COMPLETE) { "Framebuffer incomplete: 0x${status.toString(16)}" }
        } finally {
            TypesafeGL.bindFramebuffer(FramebufferId(0))
        }
    }

    companion object {
        fun create(
            width: Int,
            height: Int,
            internalFormat: TextureInternalFormat = TextureInternalFormat.RGBA8,
            depth: Boolean = false,
        ): RenderTarget {
            require(width > 0 && height > 0) { "RenderTarget size must be positive." }

            val colorTexture = Texture.empty(width, height) { this.internalFormat = internalFormat }

            val rbo = if (depth) {
                val id = TypesafeGL.genRenderbuffers()
                TypesafeGL.bindRenderbuffer(id)
                TypesafeGL.renderbufferStorage(TextureInternalFormat.DEPTH24_STENCIL8, width, height)
                id
            } else null

            return RenderTarget(width, height, colorTexture, rbo)
        }
    }

    fun bind() {
        checkOpen("render target")
        TypesafeGL.bindFramebuffer(fbo)
        TypesafeGL.viewport(0, 0, width, height)
    }

    /** Binds this target, runs [block], then restores the default framebuffer and the caller's viewport. */
    inline fun <R> use(restoreViewport: IntArray? = null, block: () -> R): R {
        bind()
        try {
            return block()
        } finally {
            TypesafeGL.bindFramebuffer(FramebufferId(0))
            restoreViewport?.let { TypesafeGL.viewport(it[0], it[1], it[2], it[3]) }
        }
    }

    override fun free() {
        TypesafeGL.deleteFramebuffers(fbo)
        depthStencilRbo?.let { TypesafeGL.deleteRenderbuffers(it) }
        colorTexture.close()
    }
}

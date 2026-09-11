package kofeychi.taksa.api.layer

import kofeychi.taksa.api.*
import kofeychi.taksa.api.texture.Texture

class Framebuffer(
    override val width: Int,
    override val height: Int,
    colorFormat: TextureInternalFormat = TextureInternalFormat.RGBA8,
    samples: Int = 1,
) : AbstractResource(), RenderTarget {
    val colorTexture = Texture.rgba(width, height) {
        internalFormat = colorFormat
        format = TextureFormat.RGBA
        dataType = TextureDataType.UBYTE
        minFilter = TextureFilter.LINEAR
        magFilter = TextureFilter.LINEAR
        wrapS = TextureWrap.CLAMP_TO_EDGE
        wrapT = TextureWrap.CLAMP_TO_EDGE
    }

    val id = TypesafeGL.genFramebuffers()
    private val depthStencil = TypesafeGL.genRenderbuffers()
    private val sampleCount = samples.coerceAtLeast(1)

    init {
        require(width > 0 && height > 0)
        require(sampleCount == 1) { "Multisampled Framebuffer requires a multisample resolve implementation; use samples=1 for the current target." }
        colorTexture.upload()
        TypesafeGL.bindFramebuffer(id)
        try {
            TypesafeGL.framebufferTexture2D(colorTexture.id)
            TypesafeGL.bindRenderbuffer(depthStencil)
            TypesafeGL.renderbufferStorage(
                TextureInternalFormat.DEPTH24_STENCIL8, width, height
            )
            TypesafeGL.framebufferRenderbuffer(org.lwjgl.opengl.GL30.GL_DEPTH_STENCIL_ATTACHMENT, depthStencil)
            check(TypesafeGL.checkFramebufferComplete()) { "Framebuffer is incomplete." }
        } finally {
            TypesafeGL.bindRenderbuffer(RenderbufferId(0))
            TypesafeGL.bindFramebuffer(FramebufferId(0))
        }
        check(sampleCount >= 1)
    }

    override fun bind() {
        check(!isClosed)
        TypesafeGL.bindFramebuffer(id)
        TypesafeGL.viewport(0, 0, width, height)
    }

    override fun unbind() {
        TypesafeGL.bindFramebuffer(FramebufferId(0))
    }

    override fun clear(r: Float, g: Float, b: Float, a: Float) {
        bind()
        TypesafeGL.clearColor(r, g, b, a)
        TypesafeGL.clear(org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT or
            org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT or
            org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT)
    }

    override fun onClose() {
        TypesafeGL.deleteRenderbuffer(depthStencil)
        TypesafeGL.deleteFramebuffer(id)
        colorTexture.close()
    }
}

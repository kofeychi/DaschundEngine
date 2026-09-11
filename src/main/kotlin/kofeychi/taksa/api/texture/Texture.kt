package kofeychi.taksa.api.texture

import kofeychi.taksa.api.*
import org.lwjgl.opengl.GL11

data class TextureConfiguration(
    var target: TextureTarget = TextureTarget.TEXTURE_2D,
    var internalFormat: TextureInternalFormat = TextureInternalFormat.RGBA8,
    var format: TextureFormat = TextureFormat.RGBA,
    var dataType: TextureDataType = TextureDataType.UBYTE,
    var minFilter: TextureFilter = TextureFilter.LINEAR,
    var magFilter: TextureFilter = TextureFilter.LINEAR,
    var wrapS: TextureWrap = TextureWrap.CLAMP_TO_EDGE,
    var wrapT: TextureWrap = TextureWrap.CLAMP_TO_EDGE,
    var generateMipmaps: Boolean = false,
    var unpackAlignment: Int = 1,
)

class Texture private constructor(
    val configuration: TextureConfiguration = TextureConfiguration(),
    contents: TextureContents? = null,
    private var ownsContents: Boolean = false,
) : AbstractResource() {
    val id: TextureId = TypesafeGL.genTextures()
    var contents: TextureContents? = contents
        private set

    private var allocatedWidth = 0
    private var allocatedHeight = 0
    private var allocatedFormat: TextureFormat? = null
    private var allocatedType: TextureDataType? = null
    private var allocatedInternal: TextureInternalFormat? = null

    init {
        check(id.id != 0) { "Could not create texture." }
        contents?.let(::validateContents)
    }

    companion object {
        fun create(contents: TextureContents? = null, action: TextureConfiguration.() -> Unit = {}) =
            Texture(TextureConfiguration().apply(action), contents, ownsContents = false)

        fun rgba(width: Int, height: Int, action: TextureConfiguration.() -> Unit = {}) =
            Texture(TextureConfiguration().apply(action), TextureContents.rgba(width, height), ownsContents = true)
    }

    fun contents(value: TextureContents): Texture {
        check(!isClosed)
        validateContents(value)
        if (ownsContents) contents?.close()
        contents = value
        ownsContents = false
        return this
    }

    fun adoptContents(value: TextureContents): Texture {
        check(!isClosed)
        validateContents(value)
        if (ownsContents) contents?.close()
        contents = value
        ownsContents = true
        return this
    }

    fun edit(action: TextureContents.() -> Unit): Texture {
        check(!isClosed)
        (contents ?: error("Texture has no contents.")).apply(action)
        return this
    }

    fun upload(): Texture {
        check(!isClosed)
        val image = contents ?: error("Texture has no contents.")
        validateContents(image)
        bind()
        try {
            configure()
            image.data.position(0).limit(image.size)
            val changed = allocatedWidth != image.width ||
                allocatedHeight != image.height ||
                allocatedFormat != image.format ||
                allocatedType != image.dataType ||
                allocatedInternal != configuration.internalFormat
            if (changed) {
                TypesafeGL.texImage2D(configuration.target, 0, configuration.internalFormat,
                    image.width, image.height, configuration.format, configuration.dataType, image.data)
                allocatedWidth = image.width
                allocatedHeight = image.height
                allocatedFormat = image.format
                allocatedType = image.dataType
                allocatedInternal = configuration.internalFormat
            } else {
                TypesafeGL.texSubImage2D(configuration.target, 0, 0, 0, image.width, image.height,
                    configuration.format, configuration.dataType, image.data)
            }
            if (configuration.generateMipmaps) TypesafeGL.generateMipmap(configuration.target)
        } finally {
            image.data.clear()
            unbind()
        }
        return this
    }

    fun upload(value: TextureContents): Texture {
        contents(value)
        return upload()
    }

    fun bind(unit: Int = TypesafeGL.state().activeTextureUnit): Texture {
        check(!isClosed)
        require(unit >= 0)
        TypesafeGL.activeTexture(unit)
        TypesafeGL.bindTexture(configuration.target, id, unit)
        return this
    }

    fun unbind(unit: Int = TypesafeGL.state().activeTextureUnit): Texture {
        require(unit >= 0)
        TypesafeGL.activeTexture(unit)
        TypesafeGL.bindTexture(configuration.target, TextureId(0), unit)
        return this
    }

    private fun configure() {
        TypesafeGL.texParameteri(configuration.target, GL11.GL_TEXTURE_MIN_FILTER, configuration.minFilter.glEnum)
        TypesafeGL.texParameteri(configuration.target, GL11.GL_TEXTURE_MAG_FILTER, configuration.magFilter.glEnum)
        TypesafeGL.texParameteri(configuration.target, GL11.GL_TEXTURE_WRAP_S, configuration.wrapS.glEnum)
        TypesafeGL.texParameteri(configuration.target, GL11.GL_TEXTURE_WRAP_T, configuration.wrapT.glEnum)
        require(configuration.unpackAlignment in setOf(1, 2, 4, 8))
        TypesafeGL.pixelStorei(GL11.GL_UNPACK_ALIGNMENT, configuration.unpackAlignment)
    }

    private fun validateContents(c: TextureContents) {
        require(c.width > 0 && c.height > 0)
        require(c.format == configuration.format) { "Expected ${configuration.format}, got ${c.format}." }
        require(c.dataType == configuration.dataType) { "Expected ${configuration.dataType}, got ${c.dataType}." }
    }

    override fun onClose() {
        contents?.let { if (ownsContents) it.close() }
        contents = null
        ownsContents = false
        val unit = TypesafeGL.state().activeTextureUnit
        TypesafeGL.activeTexture(unit)
        TypesafeGL.bindTexture(configuration.target, TextureId(0), unit)
        TypesafeGL.deleteTextures(id)
    }
}

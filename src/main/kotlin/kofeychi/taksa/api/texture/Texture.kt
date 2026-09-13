package kofeychi.taksa.api.texture

import kofeychi.taksa.api.TextureDataType
import kofeychi.taksa.api.TextureFilter
import kofeychi.taksa.api.TextureFormat
import kofeychi.taksa.api.TextureId
import kofeychi.taksa.api.TextureInternalFormat
import kofeychi.taksa.api.TextureTarget
import kofeychi.taksa.api.TextureWrap
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.util.AbstractResource
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

class Texture(
    val configuration: TextureConfiguration = TextureConfiguration(),
    contents: TextureContents? = null,
) : AbstractResource() {

    val id: TextureId = TypesafeGL.genTextures()

    var contents: TextureContents? = contents
        private set

    private var allocatedWidth = 0
    private var allocatedHeight = 0
    private var allocatedFormat: TextureFormat? = null
    private var allocatedType: TextureDataType? = null
    private var allocatedInternalFormat: TextureInternalFormat? = null

    val width: Int get() = allocatedWidth
    val height: Int get() = allocatedHeight

    init {
        check(id.id != 0) { "Could not create texture" }
        contents?.let(::validateContents)
    }

    companion object {
        fun create(contents: TextureContents? = null, action: TextureConfiguration.() -> Unit = {}): Texture {
            val configuration = TextureConfiguration().apply(action)
            return Texture(configuration, contents)
        }

        fun rgba(width: Int, height: Int, action: TextureConfiguration.() -> Unit = {}): Texture =
            create(TextureContents.rgba(width, height), action)

        /** Creates an uninitialized texture sized and formatted per [configuration] but with no CPU-side pixels - typically used as an FBO color attachment. */
        fun empty(width: Int, height: Int, action: TextureConfiguration.() -> Unit = {}): Texture {
            val texture = create(null, action)
            texture.bind()
            try {
                texture.configure()
                TypesafeGL.texImage2D(
                    texture.configuration.target, 0, texture.configuration.internalFormat,
                    width, height, texture.configuration.format, texture.configuration.dataType, null,
                )
                texture.allocatedWidth = width
                texture.allocatedHeight = height
                texture.allocatedFormat = texture.configuration.format
                texture.allocatedType = texture.configuration.dataType
                texture.allocatedInternalFormat = texture.configuration.internalFormat
            } finally {
                texture.unbind()
            }
            return texture
        }
    }

    fun contents(contents: TextureContents): Texture {
        validateContents(contents)
        this.contents = contents
        return this
    }

    fun edit(action: TextureContents.() -> Unit): Texture {
        val current = contents ?: error("Texture has no contents.")
        current.action()
        return this
    }

    fun upload(): Texture {
        checkOpen("texture")
        val image = contents ?: error("Texture has no contents to upload.")
        validateContents(image)

        bind()
        try {
            configure()
            image.data.position(0)
            image.data.limit(image.size)

            val allocationChanged = allocatedWidth != image.width ||
                allocatedHeight != image.height ||
                allocatedFormat != image.format ||
                allocatedType != image.dataType ||
                allocatedInternalFormat != configuration.internalFormat

            if (allocationChanged) {
                TypesafeGL.texImage2D(
                    configuration.target, 0, configuration.internalFormat,
                    image.width, image.height, configuration.format, configuration.dataType, image.data,
                )
                allocatedWidth = image.width
                allocatedHeight = image.height
                allocatedFormat = image.format
                allocatedType = image.dataType
                allocatedInternalFormat = configuration.internalFormat
            } else {
                TypesafeGL.texSubImage2D(
                    configuration.target, 0, 0, 0,
                    image.width, image.height, configuration.format, configuration.dataType, image.data,
                )
            }

            if (configuration.generateMipmaps) TypesafeGL.generateMipmap(configuration.target)
        } finally {
            image.data.clear()
            unbind()
        }
        return this
    }

    fun upload(contents: TextureContents): Texture {
        this.contents = contents
        return upload()
    }

    fun bind(unit: Int = 0): Texture {
        checkOpen("texture")
        require(unit >= 0) { "Texture unit must be >= 0." }
        TypesafeGL.bindTexture(configuration.target, id, unit)
        return this
    }

    fun unbind(unit: Int = 0): Texture {
        require(unit >= 0) { "Texture unit must be >= 0." }
        TypesafeGL.bindTexture(configuration.target, TextureId(0), unit)
        return this
    }

    private fun configure() {
        TypesafeGL.texParameteri(configuration.target, GL11.GL_TEXTURE_MIN_FILTER, configuration.minFilter.glEnum)
        TypesafeGL.texParameteri(configuration.target, GL11.GL_TEXTURE_MAG_FILTER, configuration.magFilter.glEnum)
        TypesafeGL.texParameteri(configuration.target, GL11.GL_TEXTURE_WRAP_S, configuration.wrapS.glEnum)
        TypesafeGL.texParameteri(configuration.target, GL11.GL_TEXTURE_WRAP_T, configuration.wrapT.glEnum)
        TypesafeGL.pixelStorei(GL11.GL_UNPACK_ALIGNMENT, configuration.unpackAlignment)
    }

    private fun validateContents(contents: TextureContents) {
        require(contents.width > 0 && contents.height > 0) { "Texture contents must have a positive size." }
        require(contents.format == configuration.format) {
            "Texture format mismatch: configuration expects ${configuration.format}, but contents use ${contents.format}."
        }
        require(contents.dataType == configuration.dataType) {
            "Texture data type mismatch: configuration expects ${configuration.dataType}, but contents use ${contents.dataType}."
        }
    }

    override fun free() {
        unbind()
        TypesafeGL.deleteTextures(id)
    }
}

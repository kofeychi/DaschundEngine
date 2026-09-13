package kofeychi.taksa.api.texture

import kofeychi.taksa.api.TextureDataType
import kofeychi.taksa.api.TextureFormat
import kofeychi.taksa.api.util.AbstractResource
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer

class TextureContents(
    val width: Int,
    val height: Int,
    val format: TextureFormat = TextureFormat.RGBA,
    val dataType: TextureDataType = TextureDataType.UBYTE,
    data: ByteBuffer? = null,
) : AbstractResource() {

    val channels: Int = when (format) {
        TextureFormat.RED, TextureFormat.RED_INTEGER -> 1
        TextureFormat.RG, TextureFormat.RG_INTEGER -> 2
        TextureFormat.RGB, TextureFormat.RGB_INTEGER -> 3
        TextureFormat.RGBA, TextureFormat.RGBA_INTEGER -> 4
        else -> error("Unsupported texture format: $format")
    }

    val size: Int = width * height * channels * dataType.bytesPerComponent

    val data: ByteBuffer = data ?: MemoryUtil.memAlloc(size)
    private val ownsMemory: Boolean = data == null

    init {
        require(width > 0) { "Texture width must be positive." }
        require(height > 0) { "Texture height must be positive." }
        require(this.data.capacity() >= size) {
            "Texture contents buffer is too small. Expected at least $size bytes, got ${this.data.capacity()}."
        }
        this.data.clear()
        this.data.limit(size)
    }

    val lastIndex: Int get() = width * height - 1

    fun index(x: Int, y: Int): Int {
        require(x in 0 until width) { "x=$x is outside 0 until $width." }
        require(y in 0 until height) { "y=$y is outside 0 until $height." }
        return (y * width + x) * channels
    }

    fun fill(value: Int = 0): TextureContents {
        require(dataType == TextureDataType.UBYTE) { "fill(Int) is only available for UBYTE texture contents." }
        checkOpen("texture contents")
        repeat(size) { data.put(it, value.toByte()) }
        return this
    }

    fun setPixel(x: Int, y: Int, vararg components: Int): TextureContents = set(x, y, *components)

    fun set(x: Int, y: Int, vararg components: Int): TextureContents {
        require(dataType == TextureDataType.UBYTE) { "set(Int...) is only available for UBYTE texture contents." }
        require(components.size == channels) { "Expected $channels component(s), got ${components.size}." }
        checkOpen("texture contents")

        val byteOffset = index(x, y)
        components.forEachIndexed { i, component -> data.put(byteOffset + i, component.coerceIn(0, 255).toByte()) }
        return this
    }

    fun pixel(x: Int, y: Int): IntArray {
        require(dataType == TextureDataType.UBYTE) { "pixel() is only available for UBYTE texture contents." }
        val byteOffset = index(x, y)
        return IntArray(channels) { data.get(byteOffset + it).toInt() and 0xFF }
    }

    fun transform(action: (x: Int, y: Int, pixel: IntArray) -> IntArray): TextureContents {
        require(dataType == TextureDataType.UBYTE) { "transform() is only available for UBYTE texture contents." }
        for (y in 0 until height) for (x in 0 until width) set(x, y, *action(x, y, pixel(x, y)))
        return this
    }

    fun setFloat(x: Int, y: Int, vararg components: Float): TextureContents {
        require(dataType == TextureDataType.FLOAT) { "setFloat(Float...) requires FLOAT texture contents." }
        require(components.size == channels) { "Expected $channels component(s), got ${components.size}." }
        checkOpen("texture contents")

        var byteOffset = index(x, y) * dataType.bytesPerComponent
        components.forEach { component ->
            data.putFloat(byteOffset, component)
            byteOffset += Float.SIZE_BYTES
        }
        return this
    }

    fun edit(action: TextureContents.() -> Unit): TextureContents {
        action()
        return this
    }

    fun copy(): TextureContents {
        val copy = TextureContents(width, height, format, dataType)
        for (i in 0 until size) copy.data.put(i, data.get(i))
        return copy
    }

    override fun free() {
        if (ownsMemory) MemoryUtil.memFree(data)
    }

    companion object {
        fun create(
            width: Int,
            height: Int,
            format: TextureFormat = TextureFormat.RGBA,
            dataType: TextureDataType = TextureDataType.UBYTE,
            action: TextureContents.() -> Unit = {},
        ): TextureContents = TextureContents(width, height, format, dataType).apply(action)

        fun rgba(width: Int, height: Int): TextureContents =
            TextureContents(width, height, TextureFormat.RGBA, TextureDataType.UBYTE)
    }
}

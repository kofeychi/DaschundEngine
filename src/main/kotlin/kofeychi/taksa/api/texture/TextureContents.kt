package kofeychi.taksa.api.texture

import kofeychi.taksa.api.AbstractResource
import kofeychi.taksa.api.TextureDataType
import kofeychi.taksa.api.TextureFormat
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
    val size: Int = Math.multiplyExact(Math.multiplyExact(width, height), channels * dataType.bytesPerComponent)
    val data: ByteBuffer = data ?: MemoryUtil.memAlloc(size)
    private val ownsMemory = data == null

    init {
        require(width > 0 && height > 0)
        require(this.data.capacity() >= size) { "Texture data capacity ${this.data.capacity()} < required $size." }
        require(this.data.isDirect) { "Texture data must be a direct ByteBuffer." }
        this.data.clear().limit(size)
    }

    fun index(x: Int, y: Int): Int {
        require(x in 0 until width && y in 0 until height)
        return (y * width + x) * channels * dataType.bytesPerComponent
    }

    fun fill(value: Int = 0): TextureContents {
        check(!isClosed)
        require(dataType == TextureDataType.UBYTE)
        for (i in 0 until size) data.put(i, value.coerceIn(0, 255).toByte())
        return this
    }

    fun set(x: Int, y: Int, vararg components: Int): TextureContents {
        check(!isClosed)
        require(dataType == TextureDataType.UBYTE)
        require(components.size == channels)
        val offset = index(x, y)
        components.forEachIndexed { i, v -> data.put(offset + i, v.coerceIn(0, 255).toByte()) }
        return this
    }

    fun pixel(x: Int, y: Int): IntArray {
        check(!isClosed)
        require(dataType == TextureDataType.UBYTE)
        val offset = index(x, y)
        return IntArray(channels) { data.get(offset + it).toInt() and 0xFF }
    }

    fun setFloat(x: Int, y: Int, vararg components: Float): TextureContents {
        check(!isClosed)
        require(dataType == TextureDataType.FLOAT)
        require(components.size == channels)
        var offset = index(x, y)
        components.forEach {
            data.putFloat(offset, it)
            offset += Float.SIZE_BYTES
        }
        return this
    }

    fun transform(action: (x: Int, y: Int, pixel: IntArray) -> IntArray): TextureContents {
        for (y in 0 until height) for (x in 0 until width) set(x, y, *action(x, y, pixel(x, y)))
        return this
    }

    fun edit(action: TextureContents.() -> Unit): TextureContents = apply(action)

    fun copy(): TextureContents {
        val copy = TextureContents(width, height, format, dataType)
        for (i in 0 until size) copy.data.put(i, data.get(i))
        return copy
    }

    override fun onClose() {
        if (ownsMemory) MemoryUtil.memFree(data)
    }

    companion object {
        fun create(width: Int, height: Int, format: TextureFormat = TextureFormat.RGBA,
                   dataType: TextureDataType = TextureDataType.UBYTE,
                   action: TextureContents.() -> Unit = {}) =
            TextureContents(width, height, format, dataType).apply(action)

        fun rgba(width: Int, height: Int, action: TextureContents.() -> Unit = {}) =
            create(width, height, TextureFormat.RGBA, TextureDataType.UBYTE, action)
    }
}

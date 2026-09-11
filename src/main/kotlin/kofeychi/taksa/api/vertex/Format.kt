package kofeychi.taksa.api.vertex

import kofeychi.taksa.api.TypesafeGL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL33

@DslMarker
annotation class FormatDSL

enum class ElementType(
    val size: Int,
    val glEnum: Int,
    val integerPointer: Boolean = false,
) {
    BYTE(1, GL11.GL_BYTE, true), UBYTE(1, GL11.GL_UNSIGNED_BYTE, true),
    SHORT(2, GL11.GL_SHORT, true), USHORT(2, GL11.GL_UNSIGNED_SHORT, true),
    INT(4, GL11.GL_INT, true), UINT(4, GL11.GL_UNSIGNED_INT, true),
    FLOAT(4, GL11.GL_FLOAT, false),
}

data class VertexAttribute(
    val index: Int,
    val type: ElementType,
    val count: Int,
    val name: String,
    val normalized: Boolean,
    val offset: Int,
    val divisor: Int = 0,
) {
    init {
        require(count in 1..4)
        require(index >= 0)
        require(offset >= 0)
        require(divisor >= 0)
    }

    val size: Int get() = type.size * count
}

typealias Element = VertexAttribute

data class Format(
    val attributes: List<VertexAttribute>,
    val stride: Int,
) {
    val elements: List<VertexAttribute> get() = attributes
    val attributesCount: Int get() = attributes.size

    /**
     * Applies vertex attribute state to the currently bound VAO.
     *
     * The caller must have a GL_ARRAY_BUFFER bound before this method is invoked;
     * glVertexAttribPointer captures that buffer binding into the VAO in core OpenGL.
     */
    fun apply(indexOffset: Int = 0) {
        attributes.forEach { attribute ->
            val index = attribute.index + indexOffset
            org.lwjgl.opengl.GL20.glEnableVertexAttribArray(index)
            if (attribute.type.integerPointer) {
                GL33.glVertexAttribIPointer(index, attribute.count, attribute.type.glEnum, stride, attribute.offset.toLong())
            } else {
                org.lwjgl.opengl.GL20.glVertexAttribPointer(
                    index, attribute.count, attribute.type.glEnum, attribute.normalized, stride, attribute.offset.toLong()
                )
            }
            GL33.glVertexAttribDivisor(index, attribute.divisor)
        }
    }

    companion object {
        inline fun builder(action: FormatBuilder.() -> Unit): Format = FormatBuilder().apply(action).build()
        fun position2UvColor(): Format = builder {
            vec2("position")
            vec2("uv")
            vec4("color")
        }
    }
}

@FormatDSL
class FormatBuilder {
    private val attrs = mutableListOf<VertexAttribute>()
    private var nextIndex = 0

    fun attribute(
        type: ElementType,
        count: Int,
        name: String,
        index: Int = nextIndex,
        normalized: Boolean = false,
        divisor: Int = 0,
    ) {
        require(index >= 0)
        require(count in 1..4)
        attrs += VertexAttribute(index, type, count, name, normalized, 0, divisor)
        nextIndex = maxOf(nextIndex, index + 1)
    }

    fun float(name: String, count: Int = 1, index: Int = nextIndex, divisor: Int = 0) =
        attribute(ElementType.FLOAT, count, name, index, false, divisor)

    fun vec2(name: String, index: Int = nextIndex, divisor: Int = 0) = float(name, 2, index, divisor)
    fun vec3(name: String, index: Int = nextIndex, divisor: Int = 0) = float(name, 3, index, divisor)
    fun vec4(name: String, index: Int = nextIndex, divisor: Int = 0) = float(name, 4, index, divisor)

    fun int(name: String, count: Int = 1, index: Int = nextIndex, normalized: Boolean = false, divisor: Int = 0) =
        attribute(ElementType.INT, count, name, index, normalized, divisor)
    fun byte(name: String, count: Int = 1, index: Int = nextIndex, normalized: Boolean = true, divisor: Int = 0) =
        attribute(ElementType.BYTE, count, name, index, normalized, divisor)

    fun build(): Format {
        require(attrs.isNotEmpty()) { "A vertex format needs at least one attribute." }
        var offset = 0
        val processed = attrs.map { original ->
            val result = original.copy(offset = offset)
            offset += original.size
            result
        }
        return Format(processed, offset)
    }
}

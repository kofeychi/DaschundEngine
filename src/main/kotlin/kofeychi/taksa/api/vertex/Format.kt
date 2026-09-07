package stellar.ether.api.rendering

import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL33.glVertexAttribDivisor
import kotlin.random.Random

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class FormatDSL

enum class ElementType(
    val size: Int,
    val glId: Int
) {
    FLOAT(4,GL_FLOAT),
    BYTE(1, GL_BYTE),
    UBYTE(1, GL_UNSIGNED_BYTE),
    SHORT(2, GL_SHORT),
    USHORT(2, GL_UNSIGNED_SHORT),
    INT(4, GL_INT),
    UINT(4, GL_UNSIGNED_INT),
}

data class Element(
    val index: Int,
    val type: ElementType,
    val count: Int,
    val name: String,
    val normalized: Boolean,
    val offset: Int,
    val divisor: Int? = null
) {
    fun size(): Int = type.size * count
}

data class Format(
    val elements: List<Element>,
    val stride: Int,
    val attributes: Int
) {
    companion object {
        fun builder(action: FormatBuilder.() -> Unit): Format {
            val b = FormatBuilder()
            b.action()
            return b.build()
        }
    }

    fun apply(
        offset: Int = 0
    ) {
        for (element in elements) {
            val idx = element.index + offset
            glEnableVertexAttribArray(idx)
            glVertexAttribPointer(
                idx,
                element.count,
                element.type.glId,
                element.normalized,
                stride,
                element.offset.toLong()
            )
            if(element.divisor != null) {
                glVertexAttribDivisor(idx, element.divisor)
            }
        }
    }
}

class FormatBuilder {
    private val elements: MutableList<Element> = mutableListOf()
    private var indx = 0

    fun element(
        index: Int = indx++,
        type: ElementType,
        count: Int,
        offset: Int,
        builder: @FormatDSL ElementBuilder.() -> Unit = {}
    ) {
        val b = ElementBuilder(
            index,
            type,
            count,
            offset,
        )
        builder(b)
        elements.add(b.build())
    }

    fun build(): Format {
        val processed = mutableListOf<Element>()
        var offset = 0

        for (element in elements) {
            val alignment = element.type.size

            val padding: Int = (alignment - (offset % alignment)) % alignment
            offset += padding

            val elementWithOffset = Element(
                element.index,
                element.type,
                element.count,
                element.name,
                element.normalized,
                offset,
                element.divisor
            )
            processed.add(elementWithOffset)

            offset += element.size()
        }

        return Format(processed,offset,processed.size)
    }
}

class ElementBuilder(
    val index: Int,
    val type: ElementType,
    val count: Int,
    val offset: Int,
) {
    companion object {
        private var id = 0
    }

    var name: String = "unknown_"+id++
    var normalized: Boolean = false
    var divisor: Int? = null

    fun name(name: String) {
        this.name = name
    }

    fun normalized(normalized: Boolean) {
        this.normalized = normalized
    }

    fun divisor(divisor: Int) {
        this.divisor = divisor
    }

    fun build(): Element = Element(
        index,
        type,
        count,
        name,
        normalized,
        offset,
        divisor
    )
}
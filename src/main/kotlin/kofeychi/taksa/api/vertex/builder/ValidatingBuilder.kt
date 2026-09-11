package kofeychi.taksa.api.vertex.builder

import kofeychi.taksa.api.vertex.ElementType
import kofeychi.taksa.api.vertex.Format

/**
 * A strict builder that tracks the exact attribute/component position for the
 * current vertex. Composite pushes write directly and are validated exactly once.
 */
class ValidatingBuilder(
    initialCapacity: Int = 4096,
    format: Format,
) : AbstractVertexBuilder(initialCapacity, format) {
    private var attributeIndex = 0
    private var componentInAttribute = 0

    private fun validateComponents(
        components: Int,
        bytesPerComponent: Int,
        expectsFloat: Boolean,
    ) {
        require(components in 1..4)
        check(attributeIndex < format.attributes.size) {
            "Too many components pushed for current vertex: attributeIndex=$attributeIndex, " +
                "attributes=${format.attributes.joinToString { it.name + "[" + it.count + "]" }}. " +
                "Do not push after completing all attributes; call push() to finish the vertex."
        }

        val attribute = format.attributes[attributeIndex]
        check(componentInAttribute + components <= attribute.count) {
            "Attribute '${attribute.name}' expects ${attribute.count} components; " +
                "received ${componentInAttribute + components}."
        }
        check(attribute.type.size == bytesPerComponent) {
            "Attribute '${attribute.name}' uses ${attribute.type}, " +
                "but the current operation writes $bytesPerComponent-byte components."
        }
        check((attribute.type == ElementType.FLOAT) == expectsFloat) {
            "Attribute '${attribute.name}' is ${attribute.type}; use a " +
                "${if (expectsFloat) "floating-point" else "integer"} push operation."
        }

        componentInAttribute += components
        if (componentInAttribute == attribute.count) {
            attributeIndex++
            componentInAttribute = 0
        }
    }

    override fun resetValidation() {
        attributeIndex = 0
        componentInAttribute = 0
    }

    override fun beginVertex() {
        check(attributeIndex == 0 && componentInAttribute == 0) {
            "Previous vertex is incomplete; call push() after supplying all attributes."
        }
    }

    override fun pushFloat(value: Float) {
        validateComponents(1, 4, true)
        writeFloat(value)
    }

    override fun pushInt(value: Int) {
        validateComponents(1, 4, false)
        writeInt(value)
    }

    override fun putByte(value: Byte) {
        validateComponents(1, 1, false)
        writeByte(value)
    }

    override fun pushFloat2(x: Float, y: Float) {
        validateComponents(2, 4, true)
        writeFloat2(x, y)
    }

    override fun pushFloat3(x: Float, y: Float, z: Float) {
        validateComponents(3, 4, true)
        writeFloat3(x, y, z)
    }

    override fun pushFloat4(x: Float, y: Float, z: Float, w: Float) {
        validateComponents(4, 4, true)
        writeFloat4(x, y, z, w)
    }

    override fun push() {
        check(attributeIndex == format.attributes.size && componentInAttribute == 0) {
            "Vertex is incomplete: stopped at attributeIndex=$attributeIndex, " +
                "componentInAttribute=$componentInAttribute of ${format.attributes.size} attributes."
        }
        check(pointer == (count + 1) * format.stride) {
            "Vertex byte count mismatch: pointer=$pointer, expected ${(count + 1) * format.stride}."
        }
        super.push()
        resetValidation()
    }

    override fun build(): Slice {
        check(attributeIndex == 0 && componentInAttribute == 0) {
            "Cannot build with an incomplete current vertex."
        }
        return super.build()
    }
}

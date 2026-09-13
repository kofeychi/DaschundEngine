package kofeychi.taksa.api.vertex.builder

import kofeychi.taksa.api.vertex.Format
import org.lwjgl.system.MemoryUtil

/**
 * Development-time builder that validates every push() against [format]: wrong
 * component counts, missing/extra pushes, and size mismatches all throw immediately
 * with a precise message instead of corrupting a mesh silently. Slower than
 * [DirectBuilder] - swap to that for release builds once the layout is verified.
 */
class ValidatingBuilder(
    initialCapacity: Int,
    format: Format,
) : AbstractVertexBuilder(initialCapacity, format) {

    private val expectedElementsPerVertex: Int = format.elements.sumOf { it.count }
    private var currentVertexElementCount: Int = 0
    private var currentElementIndex: Int = 0
    private var currentElementComponentCount: Int = 0

    override fun clear() {
        super.clear()
        currentVertexElementCount = 0
        currentElementIndex = 0
        currentElementComponentCount = 0
    }

    private fun validatePush(componentsPushed: Int) {
        if (currentElementIndex >= format.elements.size) {
            throw IllegalStateException(
                "Attempting to push data beyond the defined format structure for a single vertex. " +
                    "Expected end of vertex, but received more data."
            )
        }

        val currentElement = format.elements[currentElementIndex]

        if (currentElementComponentCount + componentsPushed > currentElement.count) {
            val expectedLeft = currentElement.count - currentElementComponentCount
            throw IllegalStateException(
                "Format mismatch for element '${currentElement.name}': " +
                    "attempted to push $componentsPushed component(s), but only $expectedLeft more are expected " +
                    "for this element (total expected for '${currentElement.name}': ${currentElement.count})."
            )
        }

        currentElementComponentCount += componentsPushed
        currentVertexElementCount += componentsPushed

        if (currentElementComponentCount == currentElement.count) {
            currentElementIndex++
            currentElementComponentCount = 0
        }
    }

    override fun push() {
        vertexCount++

        if (vertexCount.toLong() * format.stride != pointer.toLong()) {
            throw IllegalStateException("Buffer contains invalid data size. Expected: ${vertexCount * format.stride} bytes, actual: $pointer bytes.")
        }

        if (currentVertexElementCount != expectedElementsPerVertex || currentElementIndex != format.elements.size) {
            val missing = expectedElementsPerVertex - currentVertexElementCount
            val currentElName = if (currentElementIndex < format.elements.size) format.elements[currentElementIndex].name else "None"
            throw IllegalStateException("Vertex pushed prematurely. Missing $missing component(s), but stopped at $currentElName")
        }

        currentVertexElementCount = 0
        currentElementIndex = 0
        currentElementComponentCount = 0
    }

    override fun checkBuildReady() {
        super.checkBuildReady()
        check(currentVertexElementCount == 0) { "Cannot build: a vertex is currently being constructed but hasn't been push()ed." }
    }

    override fun pushFloat(value: Float) {
        validatePush(1)
        ensureCapacity(Float.SIZE_BYTES)
        MemoryUtil.memPutFloat(address + pointer, value)
        pointer += Float.SIZE_BYTES
    }

    override fun pushInt(value: Int) {
        validatePush(1)
        ensureCapacity(Int.SIZE_BYTES)
        MemoryUtil.memPutInt(address + pointer, value)
        pointer += Int.SIZE_BYTES
    }

    override fun putByte(value: Byte) {
        validatePush(1)
        ensureCapacity(Byte.SIZE_BYTES)
        MemoryUtil.memPutByte(address + pointer, value)
        pointer += Byte.SIZE_BYTES
    }

    override fun pushFloat3(x: Float, y: Float, z: Float) {
        validatePush(3)
        ensureCapacity(Float.SIZE_BYTES * 3)
        MemoryUtil.memPutFloat(address + pointer, x); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, y); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, z); pointer += Float.SIZE_BYTES
    }

    override fun pushFloat4(x: Float, y: Float, z: Float, w: Float) {
        validatePush(4)
        ensureCapacity(Float.SIZE_BYTES * 4)
        MemoryUtil.memPutFloat(address + pointer, x); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, y); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, z); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, w); pointer += Float.SIZE_BYTES
    }
}

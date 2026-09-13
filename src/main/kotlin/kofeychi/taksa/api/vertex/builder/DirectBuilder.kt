package kofeychi.taksa.api.vertex.builder

import kofeychi.taksa.api.vertex.Format
import org.lwjgl.system.MemoryUtil

/** Zero-overhead builder: no per-push format validation. Use in production hot loops once your vertex layout is known-correct (validate first with [ValidatingBuilder] during development). */
class DirectBuilder(
    initialCapacity: Int,
    format: Format,
) : AbstractVertexBuilder(initialCapacity, format) {

    override fun push() {
        vertexCount++
    }

    override fun pushFloat(value: Float) {
        ensureCapacity(Float.SIZE_BYTES)
        MemoryUtil.memPutFloat(address + pointer, value)
        pointer += Float.SIZE_BYTES
    }

    override fun pushInt(value: Int) {
        ensureCapacity(Int.SIZE_BYTES)
        MemoryUtil.memPutInt(address + pointer, value)
        pointer += Int.SIZE_BYTES
    }

    override fun putByte(value: Byte) {
        ensureCapacity(Byte.SIZE_BYTES)
        MemoryUtil.memPutByte(address + pointer, value)
        pointer += Byte.SIZE_BYTES
    }

    override fun pushFloat3(x: Float, y: Float, z: Float) {
        ensureCapacity(Float.SIZE_BYTES * 3)
        MemoryUtil.memPutFloat(address + pointer, x); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, y); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, z); pointer += Float.SIZE_BYTES
    }

    override fun pushFloat4(x: Float, y: Float, z: Float, w: Float) {
        ensureCapacity(Float.SIZE_BYTES * 4)
        MemoryUtil.memPutFloat(address + pointer, x); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, y); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, z); pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, w); pointer += Float.SIZE_BYTES
    }
}

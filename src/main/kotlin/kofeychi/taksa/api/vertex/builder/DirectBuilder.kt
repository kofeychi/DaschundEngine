package kofeychi.taksa.api.vertex.builder

import org.lwjgl.system.MemoryUtil
import stellar.ether.api.rendering.Format

class DirectBuilder(
    initialCapacity: Int,
    format: Format
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
        MemoryUtil.memPutFloat(address + pointer, x)
        pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, y)
        pointer += Float.SIZE_BYTES
        MemoryUtil.memPutFloat(address + pointer, z)
        pointer += Float.SIZE_BYTES
    }
}
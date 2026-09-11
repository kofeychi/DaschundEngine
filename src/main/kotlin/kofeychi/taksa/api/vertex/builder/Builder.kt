package kofeychi.taksa.api.vertex.builder

import kofeychi.taksa.api.AbstractResource
import kofeychi.taksa.api.CleanableResource
import kofeychi.taksa.api.NativeMemory
import kofeychi.taksa.api.vertex.Format
import kotlin.math.max

/**
 * A Slice owns memory by default. Borrowed views are safe to close repeatedly,
 * but closing a borrowed view never frees the builder's backing allocation.
 */
class Slice internal constructor(
    internal var address: Long,
    val size: Int,
    val vertexCount: Int,
    val format: Format,
    private val ownsMemory: Boolean,
    private val owner: CleanableResource? = null,
) : AbstractResource() {
    override fun onClose() {
        if (ownsMemory) {
            NativeMemory.free(address)
            address = 0L
        }
    }

    internal fun requireOpen() {
        check(!isClosed && address != 0L) { "Slice is closed." }
        check(owner?.isClosed != true) { "Slice owner has been closed." }
    }
}

interface Builder : AutoCloseable {
    val format: Format
    val size: Int
    val vertexCount: Int

    fun clear()
    fun beginVertex()
    fun push()
    fun pushFloat(value: Float)
    fun pushInt(value: Int)
    fun putByte(value: Byte)
    fun pushFloat2(x: Float, y: Float)
    fun pushFloat3(x: Float, y: Float, z: Float)
    fun pushFloat4(x: Float, y: Float, z: Float, w: Float)

    fun build(): Slice
    fun view(): Slice

    fun <R> build(use: (Slice) -> R): R = build().use(use)
}

abstract class AbstractVertexBuilder(
    initialCapacity: Int,
    final override val format: Format,
) : AbstractResource(), Builder {
    protected var address: Long = 0L
    protected var capacity: Int = max(initialCapacity, format.stride)
    protected var pointer: Int = 0
    protected var count: Int = 0

    override val size: Int get() = pointer
    override val vertexCount: Int get() = count

    init {
        address = NativeMemory.allocate(capacity.toLong())
    }

    protected fun ensureCapacity(extraBytes: Int) {
        check(!isClosed)
        require(extraBytes >= 0)
        val required = pointer.toLong() + extraBytes
        if (required <= capacity) return
        var next = capacity
        while (next < required) next = max(next * 2, 64)
        address = NativeMemory.reallocate(address, next.toLong())
        capacity = next
    }

    override fun clear() {
        check(!isClosed)
        pointer = 0
        count = 0
        resetValidation()
    }

    protected open fun resetValidation() = Unit

    override fun beginVertex() = Unit
    override fun push() {
        check(pointer % format.stride == 0) { "Vertex data size $pointer is not aligned to stride ${format.stride}." }
        count++
    }

    protected fun writeFloat(value: Float) {
        ensureCapacity(4)
        org.lwjgl.system.MemoryUtil.memPutFloat(address + pointer, value)
        pointer += 4
    }

    protected fun writeInt(value: Int) {
        ensureCapacity(4)
        org.lwjgl.system.MemoryUtil.memPutInt(address + pointer, value)
        pointer += 4
    }

    protected fun writeByte(value: Byte) {
        ensureCapacity(1)
        org.lwjgl.system.MemoryUtil.memPutByte(address + pointer, value)
        pointer++
    }

    protected fun writeFloat2(x: Float, y: Float) {
        writeFloat(x)
        writeFloat(y)
    }

    protected fun writeFloat3(x: Float, y: Float, z: Float) {
        writeFloat(x)
        writeFloat(y)
        writeFloat(z)
    }

    protected fun writeFloat4(x: Float, y: Float, z: Float, w: Float) {
        writeFloat(x)
        writeFloat(y)
        writeFloat(z)
        writeFloat(w)
    }

    override fun pushFloat(value: Float) = writeFloat(value)
    override fun pushInt(value: Int) = writeInt(value)
    override fun putByte(value: Byte) = writeByte(value)
    override fun pushFloat2(x: Float, y: Float) = writeFloat2(x, y)
    override fun pushFloat3(x: Float, y: Float, z: Float) = writeFloat3(x, y, z)
    override fun pushFloat4(x: Float, y: Float, z: Float, w: Float) = writeFloat4(x, y, z, w)

    override fun build(): Slice {
        check(!isClosed)
        check(count > 0) { "Builder contains no vertices." }
        check(pointer == count * format.stride) {
            "Builder contains $pointer bytes for $count vertices; expected ${count * format.stride}."
        }
        val result = NativeMemory.allocate(pointer.toLong())
        org.lwjgl.system.MemoryUtil.memCopy(address, result, pointer.toLong())
        val slice = Slice(result, pointer, count, format, ownsMemory = true)
        pointer = 0
        count = 0
        resetValidation()
        return slice
    }

    override fun view(): Slice {
        check(!isClosed)
        check(count > 0)
        check(pointer == count * format.stride)
        return Slice(address, pointer, count, format, ownsMemory = false, owner = this)
    }

    override fun onClose() {
        NativeMemory.free(address)
        address = 0L
    }
}

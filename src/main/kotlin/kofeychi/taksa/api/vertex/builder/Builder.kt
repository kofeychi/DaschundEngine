package kofeychi.taksa.api.vertex.builder

import org.lwjgl.system.MemoryUtil
import stellar.ether.api.rendering.Format
import java.io.Closeable
import kotlin.math.max

class Slice(
    var address: Long,
    val size: Int,
    val vertexCount: Int,
    val format: Format,
    private val ownsMemory: Boolean = true
) : AutoCloseable {

    override fun close() {
        if (address != 0L) {
            if (ownsMemory) {
                MemoryUtil.nmemFree(address)
            }
            address = 0L
        } else {
            throw IllegalStateException("Tried to close an already closed Built object")
        }
    }
}

interface Builder : Closeable {

    val format: Format

    fun clear()
    fun push()

    fun pushFloat(value: Float)
    fun pushInt(value: Int)
    fun putByte(value: Byte)
    fun pushFloat3(x: Float, y: Float, z: Float)

    fun build(): Slice
    fun view(): Slice
}

abstract class AbstractVertexBuilder(
    initialCapacity: Int,
    override val format: Format
) : Builder {

    var address: Long = 0L
        protected set
    var capacity: Int = 0
        protected set
    var pointer: Int = 0
        protected set
    var vertexCount: Int = 0
        protected set

    init {
        val startingCapacity = max(format.stride, initialCapacity)
        this.capacity = startingCapacity
        this.address = MemoryUtil.nmemAlloc(startingCapacity.toLong())
        check(this.address != 0L) { "Buffer was not able to allocate" }
    }

    protected fun ensureCapacity(additionalBytes: Int) {
        check(address != 0L) { "Buffer is not allocated" }

        val requiredCapacity = pointer + additionalBytes
        if (requiredCapacity > capacity) {
            var newCapacity = max(capacity + (capacity shr 1), 64)

            if (requiredCapacity > newCapacity) {
                newCapacity = requiredCapacity + format.stride
            }

            address = MemoryUtil.nmemRealloc(address, newCapacity.toLong())
            if (address == 0L) {
                throw OutOfMemoryError("Failed to reallocate native memory to $newCapacity bytes.")
            }
            capacity = newCapacity
        }
    }

    override fun close() {
        if (address != 0L) {
            MemoryUtil.nmemFree(address)
            address = 0L
        }
    }

    protected open fun checkBuildReady() {
        check(vertexCount != 0) { "Buffer has no vertices, did you forget push()?" }
    }

    override fun build(): Slice {
        checkBuildReady()

        val finishedAddress = MemoryUtil.nmemAlloc(pointer.toLong())
        if (finishedAddress == 0L) {
            throw OutOfMemoryError("Failed to allocate native memory to build a mesh ($pointer bytes)")
        }

        MemoryUtil.memCopy(address, finishedAddress, pointer.toLong())
        val built = Slice(finishedAddress, pointer, vertexCount, format)

        clear()
        close()

        return built
    }

    override fun view(): Slice {
        checkBuildReady()
        return Slice(address, pointer, vertexCount, format, ownsMemory = false)
    }

    override fun clear() {
        pointer = 0
        vertexCount = 0
    }
}
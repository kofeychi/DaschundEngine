package kofeychi.taksa.api.vertex.builder

import kofeychi.taksa.api.vertex.Format
import org.lwjgl.system.MemoryUtil
import kotlin.math.max

/**
 * A finished, GPU-uploadable run of vertex bytes. `ownsMemory = true` slices free their
 * native memory on close(); `ownsMemory = false` "view" slices (see [Builder.view]) are
 * just a window into memory the builder itself still owns, so closing them is a no-op
 * for the memory (but still marks the slice unusable, catching use-after-close bugs).
 */
class Slice(
    var address: Long,
    val size: Int,
    val vertexCount: Int,
    val format: Format,
    private val ownsMemory: Boolean = true,
) : AutoCloseable {

    val isClosed: Boolean get() = address == 0L

    override fun close() {
        if (address == 0L) return
        if (ownsMemory) MemoryUtil.nmemFree(address)
        address = 0L
    }
}

interface Builder : AutoCloseable {

    val format: Format

    fun clear()
    fun push()

    fun pushFloat(value: Float)
    fun pushInt(value: Int)
    fun putByte(value: Byte)
    fun pushFloat3(x: Float, y: Float, z: Float)
    fun pushFloat4(x: Float, y: Float, z: Float, w: Float)

    /** Copies the currently-built bytes into a brand-new owned [Slice] and resets/frees this builder's staging memory. */
    fun build(): Slice

    /** Non-owning window into the builder's own staging memory - zero-copy, but only valid until the next mutation of this builder. */
    fun view(): Slice

    fun <R> build(use: (Slice) -> R): R = build().use(use)
}

abstract class AbstractVertexBuilder(
    initialCapacity: Int,
    override val format: Format,
) : Builder {

    var address: Long = 0L
        protected set
    var capacity: Int = 0
        protected set
    var pointer: Int = 0
        protected set
    var vertexCount: Int = 0
        protected set

    private var closed = false

    init {
        require(format.stride > 0) { "Format must have at least one element (stride was 0)." }
        val startingCapacity = max(format.stride, initialCapacity)
        capacity = startingCapacity
        address = MemoryUtil.nmemAlloc(startingCapacity.toLong())
        check(address != 0L) { "Buffer was not able to allocate" }
    }

    protected fun ensureCapacity(additionalBytes: Int) {
        check(address != 0L) { "Buffer is not allocated (was it already build()/close()d?)" }

        val requiredCapacity = pointer + additionalBytes
        if (requiredCapacity > capacity) {
            var newCapacity = max(capacity + (capacity shr 1), 64)
            if (requiredCapacity > newCapacity) newCapacity = requiredCapacity + format.stride

            val reallocated = MemoryUtil.nmemRealloc(address, newCapacity.toLong())
            if (reallocated == 0L) throw OutOfMemoryError("Failed to reallocate native memory to $newCapacity bytes.")
            address = reallocated
            capacity = newCapacity
        }
    }

    override fun close() {
        if (closed) return
        closed = true
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
        if (finishedAddress == 0L) throw OutOfMemoryError("Failed to allocate native memory to build a mesh ($pointer bytes)")

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

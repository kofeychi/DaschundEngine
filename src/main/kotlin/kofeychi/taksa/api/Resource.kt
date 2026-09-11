package kofeychi.taksa.api

import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.util.ArrayDeque

/**
 * Deterministically closable resource. `close` is idempotent.
 *
 * OpenGL objects are intentionally not cleaned from a JVM Cleaner thread because
 * GL deletion must happen while the correct OpenGL context is current.
 */
interface CleanableResource : AutoCloseable {
    val isClosed: Boolean
    override fun close()
}

abstract class AbstractResource : CleanableResource {
    @Volatile
    final override var isClosed: Boolean = false
        private set

    final override fun close() {
        if (isClosed) return
        synchronized(this) {
            if (isClosed) return
            try {
                onClose()
            } finally {
                isClosed = true
            }
        }
    }

    protected abstract fun onClose()
}

object NativeMemory {
    fun allocate(bytes: Long): Long {
        require(bytes > 0) { "Native allocation size must be > 0." }
        val address = MemoryUtil.nmemAlloc(bytes)
        check(address != 0L) { "Native allocation failed for $bytes bytes." }
        return address
    }

    fun calloc(bytes: Long): Long {
        require(bytes > 0) { "Native allocation size must be > 0." }
        val address = MemoryUtil.nmemCalloc(1, bytes)
        check(address != 0L) { "Native allocation failed for $bytes bytes." }
        return address
    }

    fun reallocate(address: Long, bytes: Long): Long {
        require(address != 0L) { "Cannot realloc a null address." }
        require(bytes > 0) { "Native allocation size must be > 0." }
        val newAddress = MemoryUtil.nmemRealloc(address, bytes)
        check(newAddress != 0L) { "Native reallocation failed for $bytes bytes." }
        return newAddress
    }

    fun free(address: Long) {
        if (address != 0L) MemoryUtil.nmemFree(address)
    }

    inline fun <R> stack(block: MemoryStack.() -> R): R =
        MemoryStack.stackPush().use(block)
}

class ResourceScope : AbstractResource() {
    private val resources = ArrayDeque<AutoCloseable>()

    fun <R : AutoCloseable> own(resource: R): R {
        check(!isClosed)
        resources.addLast(resource)
        return resource
    }

    inline fun <reified R : AutoCloseable> own(factory: () -> R): R = own(factory())

    override fun onClose() {
        var failure: Throwable? = null
        while (resources.isNotEmpty()) {
            try { resources.removeLast().close() }
            catch (t: Throwable) { failure = failure?.also { it.addSuppressed(t) } ?: t }
        }
        if (failure != null) throw failure!!
    }
}

inline fun <R> scopedResources(block: ResourceScope.() -> R): R =
    ResourceScope().use(block)

class NativeArena(initialCapacity: Int = 64 * 1024) : AbstractResource() {
    private val blocks = ArrayList<Long>()
    private var current = 0L
    private var offset = 0L
    private var capacity = initialCapacity.coerceAtLeast(1024).toLong()

    private fun ensure(bytes: Long) {
        if (current == 0L || offset + bytes > capacity) {
            if (current != 0L) blocks += current
            capacity = maxOf(capacity * 2, bytes)
            current = NativeMemory.allocate(capacity)
            offset = 0L
        }
    }

    fun alloc(bytes: Long, alignment: Long = 8L): Long {
        require(bytes > 0)
        require(alignment > 0 && alignment and (alignment - 1) == 0L)
        val mask = alignment - 1
        ensure(bytes + mask)
        val aligned = (offset + mask) and mask.inv()
        val result = current + aligned
        offset = aligned + bytes
        return result
    }

    override fun onClose() {
        NativeMemory.free(current)
        current = 0L
        blocks.forEach(NativeMemory::free)
        blocks.clear()
        offset = 0L
    }
}

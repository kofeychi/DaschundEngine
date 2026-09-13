package kofeychi.taksa.api.util

import org.lwjgl.system.MemoryStack

/**
 * Any GL/native resource that can be released exactly once.
 * Unlike plain [AutoCloseable], double-close() is a no-op instead of a crash,
 * which makes cleanup in `finally` blocks and GC-adjacent paths safe.
 */
interface CleanableResource : AutoCloseable {
    val isClosed: Boolean
}

/**
 * Base class implementing the "close exactly once" contract.
 * Subclasses put actual GL delete calls in [free]; [free] is guaranteed
 * to run at most once even if close() is called multiple times or concurrently.
 */
abstract class AbstractResource : CleanableResource {
    @Volatile
    private var closed = false

    override val isClosed: Boolean get() = closed

    protected fun checkOpen(what: String = "resource") {
        check(!closed) { "$what is already closed and can no longer be used." }
    }

    protected abstract fun free()

    final override fun close() {
        if (closed) return
        synchronized(this) {
            if (closed) return
            free()
            closed = true
        }
    }
}

/**
 * Runs [block] inside a scoped stack allocation region. Every stack allocation made
 * through the given [MemoryStack] inside [block] is freed automatically the instant
 * the block returns (LIFO), guaranteeing zero leaks for short-lived per-frame data
 * (small uniform arrays, temporary vertex batches, etc). Prefer this over manual
 * [org.lwjgl.system.MemoryUtil] alloc/free pairs whenever the buffer does not need
 * to outlive the current call.
 */
inline fun <R> scoped(block: (MemoryStack) -> R): R = MemoryStack.stackPush().use(block)

/**
 * Closes every resource in [resources], even if some of them throw while closing.
 * The first exception encountered is rethrown after all resources have been given
 * a chance to release their native memory / GL handles; subsequent exceptions are
 * attached as suppressed. Use this instead of chained `.use {}` when you have a
 * dynamic collection of resources (e.g. all layers in a stack) that must all be
 * torn down regardless of individual failures.
 */
fun closeAll(resources: Iterable<AutoCloseable?>) {
    var primary: Throwable? = null
    for (resource in resources) {
        try {
            resource?.close()
        } catch (t: Throwable) {
            if (primary == null) primary = t else primary.addSuppressed(t)
        }
    }
    primary?.let { throw it }
}

fun closeAll(vararg resources: AutoCloseable?) = closeAll(resources.asList())

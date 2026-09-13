package kofeychi.taksa.api

import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Configuration

object DebugRunner {
    /**
     * Enables LWJGL's debug allocator/loader/stack checks, installs a GL debug
     * message callback for the lifetime of [action], and always tears it down
     * afterwards - even if [action] throws.
     */
    fun run(init: () -> Unit, action: () -> Unit) {
        Configuration.DEBUG_FUNCTIONS.set(true)
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(true)
        Configuration.DEBUG_LOADER.set(true)
        Configuration.DEBUG_STACK.set(true)
        Configuration.DEBUG.set(true)
        init()
        GLUtil.setupDebugMessageCallback()?.use {
            action()
        } ?: action()
    }
}

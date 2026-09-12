package kofeychi.taksa.api

import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Configuration

object DebugRunner {
    fun run(
        init: () -> Unit,
        action: () -> Unit
    ) {
        Configuration.DEBUG_FUNCTIONS.set(true)
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(true)
        Configuration.DEBUG_LOADER.set(true)
        Configuration.DEBUG_STACK.set(true)
        Configuration.DEBUG.set(true)
        init()
        GLUtil.setupDebugMessageCallback()?.close()
        action()
    }
}
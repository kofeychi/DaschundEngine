package kofeychi.taksa.api

import org.lwjgl.glfw.GLFW
import java.util.concurrent.locks.LockSupport
import java.util.function.DoubleSupplier

class FpsController(private val targetFps: DoubleSupplier) {
    private val targetFrameTime: Double = if (targetFps.asDouble > 0) 1.0 / targetFps.asDouble else 0.0
    private var lastTime: Double = GLFW.glfwGetTime()

    /** Delta time of the last frame, in seconds. */
    var deltaTime: Float = 0f
        private set

    /** Delta time of the last frame, in nanoseconds - handy for [kofeychi.taksa.api.layer.DrawCtx]. */
    val deltaNanos: Long get() = (deltaTime.toDouble() * 1_000_000_000.0).toLong()

    /** Smoothed instantaneous FPS derived from the last frame's delta. */
    val fps: Double get() = if (deltaTime > 0f) 1.0 / deltaTime else 0.0

    fun update() {
        val currentTime = GLFW.glfwGetTime()
        deltaTime = (currentTime - lastTime).toFloat()
        lastTime = currentTime
    }

    fun sync() {
        if (targetFps.asDouble <= 0) return
        val syncTime = lastTime + targetFrameTime
        while (GLFW.glfwGetTime() < syncTime) {
            LockSupport.parkNanos(500_000L)
        }
    }
}

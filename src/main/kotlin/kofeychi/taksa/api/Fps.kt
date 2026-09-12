package kofeychi.taksa.api

import org.lwjgl.glfw.GLFW
import java.util.concurrent.locks.LockSupport
import java.util.function.DoubleSupplier

class FpsController(private val targetFps: DoubleSupplier) {
    private val targetFrameTime: Double = if (targetFps.asDouble > 0) 1.0 / targetFps.asDouble else 0.0
    private var lastTime: Double
    var deltaTime: Float = 0f
        private set

    init {
        this.lastTime = GLFW.glfwGetTime()
    }

    fun update() {
        val currentTime = GLFW.glfwGetTime()
        deltaTime = (currentTime - lastTime).toFloat()
        lastTime = currentTime
    }

    fun sync() {
        if (targetFps.asDouble <= 0) return
        val syncTime = lastTime + targetFrameTime
        while (GLFW.glfwGetTime() < syncTime) {
            LockSupport.parkNanos(500000L)
        }
    }
}
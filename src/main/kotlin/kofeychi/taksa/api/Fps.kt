package kofeychi.taksa.api

import org.lwjgl.glfw.GLFW
import java.util.concurrent.locks.LockSupport
import java.util.function.DoubleSupplier

data class FrameTiming(
    val deltaNanos: Long,
    val elapsedNanos: Long,
    val fps: Double,
) {
    val deltaSeconds: Float get() = deltaNanos / 1_000_000_000f
}

class FrameClock {
    private var lastNanos = System.nanoTime()
    private var elapsedNanos = 0L
    private var smoothedDelta = 1.0 / 60.0

    fun tick(nowNanos: Long = System.nanoTime()): FrameTiming {
        val delta = (nowNanos - lastNanos).coerceAtLeast(0L)
        lastNanos = nowNanos
        elapsedNanos += delta
        val seconds = delta / 1_000_000_000.0
        smoothedDelta = smoothedDelta * 0.9 + seconds * 0.1
        return FrameTiming(delta, elapsedNanos, if (smoothedDelta > 0.0) 1.0 / smoothedDelta else 0.0)
    }

    fun reset(nowNanos: Long = System.nanoTime()) {
        lastNanos = nowNanos
        elapsedNanos = 0L
    }
}

class FpsController(private val targetFps: DoubleSupplier) {
    private var lastTime = GLFW.glfwGetTime()
    private var targetFrameTime = 0.0
    var deltaTime: Float = 0f
        private set

    init { recompute() }

    private fun recompute() {
        targetFrameTime = targetFps.asDouble.takeIf { it > 0.0 }?.let { 1.0 / it } ?: 0.0
    }

    fun update() {
        val now = GLFW.glfwGetTime()
        deltaTime = (now - lastTime).toFloat().coerceAtLeast(0f)
        lastTime = now
        recompute()
    }

    fun sync() {
        if (targetFrameTime <= 0.0) return
        val deadline = lastTime + targetFrameTime
        while (GLFW.glfwGetTime() < deadline) LockSupport.parkNanos(200_000L)
    }
}

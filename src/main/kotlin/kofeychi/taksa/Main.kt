package kofeychi.taksa

import kofeychi.taksa.api.BufferUsage
import kofeychi.taksa.api.DebugRunner
import kofeychi.taksa.api.ResourceScope
import kofeychi.taksa.api.ShaderType
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.layer.DefaultFramebuffer
import kofeychi.taksa.api.layer.Framebuffer
import kofeychi.taksa.api.shader.Program
import kofeychi.taksa.api.shader.Shader
import kofeychi.taksa.api.shader.ShaderSource
import kofeychi.taksa.api.texture.Texture
import kofeychi.taksa.api.vertex.Format
import kofeychi.taksa.api.vertex.buffer.Mesh
import kofeychi.taksa.api.vertex.buffer.QuadBatch
import kofeychi.taksa.api.vertex.builder.DirectBuilder
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil

private const val WIDTH = 1280
private const val HEIGHT = 720

fun main() {
    DebugRunner.run(
        init = { DemoWindow.init() },
        action = { DemoApp().run() },
    )
}

private object DemoWindow {
    var window: Long = 0L
        private set

    fun init() {
        GLFWErrorCallback.createPrint(System.err).set()
        check(GLFW.glfwInit()) { "Unable to initialise GLFW." }

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)
        if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE)
        }

        window = GLFW.glfwCreateWindow(
            WIDTH,
            HEIGHT,
            "Taksa API — 3D Framebuffer + Post FX",
            MemoryUtil.NULL,
            MemoryUtil.NULL,
        )
        check(window != MemoryUtil.NULL) { "Unable to create GLFW window." }
        GLFW.glfwMakeContextCurrent(window)
        GLFW.glfwSwapInterval(1)
        GL.createCapabilities()
        TypesafeGL.invalidateState()
    }

    fun close() {
        if (window != MemoryUtil.NULL) GLFW.glfwDestroyWindow(window)
        window = MemoryUtil.NULL
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)?.free()
    }
}

private class DemoApp {
    fun run() {
        val failure = try {
            runLoop()
            null
        } catch (t: Throwable) {
            t
        } finally {
            DemoWindow.close()
        }
        failure?.let { throw it }
    }

    private fun runLoop() = ResourceScope().use { scope ->
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_LEQUAL)
        GL11.glDisable(GL11.GL_BLEND)

        val cubeProgram = createProgram(scope, CUBE_VERTEX_SHADER, CUBE_FRAGMENT_SHADER)
        val postProgram = createProgram(scope, POST_VERTEX_SHADER, POST_FRAGMENT_SHADER)

        val cubeTexture = scope.own(createCheckerTexture())
        val cube = scope.own(createCubeMesh())
        val postQuad = scope.own(
            QuadBatch(maxQuads = 1, format = Format.position2UvColor(), usage = BufferUsage.STREAM_DRAW)
        )

        val offscreen = scope.own(Framebuffer(WIDTH, HEIGHT))
        val windowTarget = DefaultFramebuffer { kofeychi.taksa.api.layer.IntRect(0, 0, WIDTH, HEIGHT) }

        val projection = Matrix4f().perspective(
            Math.toRadians(55.0).toFloat(),
            WIDTH.toFloat() / HEIGHT.toFloat(),
            0.1f,
            100f,
        )
        val view = Matrix4f().lookAt(
            0f, 0.4f, 5.3f,
            0f, 0.2f, 0f,
            0f, 1f, 0f,
        )

        val startNanos = System.nanoTime()
        val model = Matrix4f()
        val projectionView = Matrix4f(projection).mul(view)

        while (!GLFW.glfwWindowShouldClose(DemoWindow.window)) {
            if (GLFW.glfwGetKey(DemoWindow.window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
                GLFW.glfwSetWindowShouldClose(DemoWindow.window, true)
            }

            val elapsed = (System.nanoTime() - startNanos) / 1_000_000_000f

            // Pass 1: render the textured, rotating cube into an offscreen framebuffer.
            offscreen.bind()
            try {
                offscreen.clear(0.015f, 0.02f, 0.035f, 1f)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glViewport(0, 0, WIDTH, HEIGHT)

                cubeProgram.bind()
                cubeProgram.uniform("uProjectionView").set(projectionView)
                cubeProgram.uniform("uTime").set(elapsed)
                cubeProgram.uniform("uTexture").setTextureUnit(0)

                cubeTexture.bind(0)
                try {
                    cube.bind()
                    try {
                        cube.draw()
                    } finally {
                        cube.unbind()
                    }
                } finally {
                    cubeTexture.unbind(0)
                }
                cubeProgram.unbind()
            } finally {
                offscreen.unbind()
            }

            // Pass 2: draw the framebuffer through a post-processing shader.
            windowTarget.bind()
            try {
                windowTarget.clear(0f, 0f, 0f, 1f)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glViewport(0, 0, WIDTH, HEIGHT)

                postProgram.bind()
                postProgram.uniform("uTexture").setTextureUnit(0)
                postProgram.uniform("uTime").set(elapsed)

                offscreen.colorTexture.bind(0)
                try {
                    postQuad.begin()
                    postQuad.quad(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat())
                    postQuad.flush()
                } finally {
                    offscreen.colorTexture.unbind(0)
                }
                postProgram.unbind()
            } finally {
                windowTarget.unbind()
            }

            GLFW.glfwSwapBuffers(DemoWindow.window)
            GLFW.glfwPollEvents()
        }
    }

    private fun createProgram(scope: ResourceScope, vertexSource: String, fragmentSource: String): Program {
        val vertex = scope.own(Shader(ShaderType.VERTEX, ShaderSource(vertexSource)))
        val fragment = scope.own(Shader(ShaderType.FRAGMENT, ShaderSource(fragmentSource)))
        return scope.own(Program.linked(vertex, fragment))
    }

    private fun createCheckerTexture(): Texture {
        val texture = Texture.rgba(256, 256) {
            minFilter = kofeychi.taksa.api.TextureFilter.LINEAR_MIPMAP_LINEAR
            magFilter = kofeychi.taksa.api.TextureFilter.LINEAR
            wrapS = kofeychi.taksa.api.TextureWrap.REPEAT
            wrapT = kofeychi.taksa.api.TextureWrap.REPEAT
            generateMipmaps = true
        }
        texture.edit {
            val size = 32
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val cell = ((x / size) + (y / size)) and 1
                    if (cell == 0) {
                        set(x, y, 244, 238, 210, 255)
                    } else {
                        set(x, y, 42, 78, 136, 255)
                    }
                }
            }
        }.upload()
        return texture
    }

    private fun createCubeMesh(): Mesh {
        val format = Format.builder {
            vec3("position")
            vec2("uv")
        }
        val builder = DirectBuilder(initialCapacity = 36 * format.stride, format = format)

        val faces = arrayOf(
            Face(intArrayOf(4, 5, 6, 7)),
            Face(intArrayOf(1, 0, 3, 2)),
            Face(intArrayOf(3, 2, 6, 7)),
            Face(intArrayOf(0, 1, 5, 4)),
            Face(intArrayOf(2, 3, 7, 6)),
            Face(intArrayOf(0, 4, 5, 1)),
        )
        val positions = arrayOf(
            floatArrayOf(-1f, -1f, -1f),
            floatArrayOf(+1f, -1f, -1f),
            floatArrayOf(+1f, +1f, -1f),
            floatArrayOf(-1f, +1f, -1f),
            floatArrayOf(-1f, -1f, +1f),
            floatArrayOf(+1f, -1f, +1f),
            floatArrayOf(+1f, +1f, +1f),
            floatArrayOf(-1f, +1f, +1f),
        )
        val uv = arrayOf(
            floatArrayOf(0f, 0f),
            floatArrayOf(1f, 0f),
            floatArrayOf(1f, 1f),
            floatArrayOf(0f, 1f),
        )

        fun vertex(index: Int, uvIndex: Int) {
            val p = positions[index]
            val t = uv[uvIndex]
            builder.beginVertex()
            builder.pushFloat3(p[0], p[1], p[2])
            builder.pushFloat2(t[0], t[1])
            builder.push()
        }

        faces.forEach { face ->
            val q = face.corners
            vertex(q[0], 0)
            vertex(q[1], 1)
            vertex(q[2], 2)
            vertex(q[0], 0)
            vertex(q[2], 2)
            vertex(q[3], 3)
        }

        val mesh = Mesh(format)
        builder.build { slice -> mesh.upload(slice) }
        builder.close()
        return mesh
    }

    private data class Face(val corners: IntArray)
}

private const val CUBE_VERTEX_SHADER = """
#version 330 core
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aUv;

uniform mat4 uProjectionView;
uniform float uTime;

out vec2 vUv;

mat3 rotationY(float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return mat3(
        c, 0.0, -s,
        0.0, 1.0, 0.0,
        s, 0.0, c
    );
}

mat3 rotationX(float angle) {
    float c = cos(angle);
    float s = sin(angle);
    return mat3(
        1.0, 0.0, 0.0,
        0.0, c, s,
        0.0, -s, c
    );
}

void main() {
    float y = uTime * 0.9;
    float x = uTime * 0.53;
    vec3 rotated = rotationY(y) * rotationX(x) * aPosition;
    rotated *= 1.15;
    gl_Position = uProjectionView * vec4(rotated, 1.0);
    vUv = aUv;
}
"""

private const val CUBE_FRAGMENT_SHADER = """
#version 330 core
in vec2 vUv;
uniform sampler2D uTexture;
out vec4 fragColor;

void main() {
    vec3 texel = texture(uTexture, vUv).rgb;
    vec3 lit = texel * vec3(1.0, 0.92, 0.86);
    fragColor = vec4(lit, 1.0);
}
"""

private const val POST_VERTEX_SHADER = """
#version 330 core
layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec2 aUv;
layout(location = 2) in vec4 aColor;

out vec2 vUv;
out vec4 vColor;

void main() {
    vec2 ndc = vec2(
        aPosition.x / 640.0 - 1.0,
        aPosition.y / 360.0 - 1.0
    );
    gl_Position = vec4(ndc, 0.0, 1.0);
    vUv = aUv;
    vColor = aColor;
}
"""

private const val POST_FRAGMENT_SHADER = """
#version 330 core
in vec2 vUv;
in vec4 vColor;
uniform sampler2D uTexture;
uniform float uTime;
out vec4 fragColor;

void main() {
    vec2 p = vUv - vec2(0.5);
    float dist = length(p);
    float vignette = smoothstep(0.82, 0.28, dist);

    float wave = sin(uTime * 1.8 + vUv.y * 18.0) * 0.0018;
    float red = texture(uTexture, vUv + vec2(wave, 0.0)).r;
    float green = texture(uTexture, vUv).g;
    float blue = texture(uTexture, vUv - vec2(wave, 0.0)).b;

    vec3 color = vec3(red, green, blue);
    color = pow(max(color, vec3(0.0)), vec3(0.92));
    color *= vignette;

    fragColor = vec4(color, 1.0) * vColor;
}
"""

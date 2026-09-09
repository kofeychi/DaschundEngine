package kofeychi.taksa

import kofeychi.taksa.api.*
import kofeychi.taksa.api.shader.*
import kofeychi.taksa.api.vertex.*
import kofeychi.taksa.api.vertex.buffer.Mesh
import kofeychi.taksa.api.vertex.builder.*
import org.joml.*
import org.lwjgl.glfw.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GLUtil
import org.lwjgl.system.Configuration
import org.lwjgl.system.MemoryUtil

fun Builder.pushFloat3(mat: Matrix4f, vec: Vector3f) {
    val vecc = mat.transform(Vector4f(vec.x,vec.y,vec.z,1f)).xyz(vec)
    pushFloat3(vecc.x,vecc.y,vecc.z)
}

val formatEbanat = Format.builder {
    element(
        0,
        ElementType.FLOAT,
        3,
        0
    )
    element(
        1,
        ElementType.FLOAT,
        4,
        0
    )
}

object Taksa {
    var window = 0L

    fun init() {
        GLFWErrorCallback.createPrint(System.err).set()

        if(!GLFW.glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        GLFW.glfwDefaultWindowHints()
        glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)

        val window = GLFW.glfwCreateWindow(870, 870, "ukraina", MemoryUtil.NULL, MemoryUtil.NULL)

        if (window == MemoryUtil.NULL) {
            throw RuntimeException("Не удалось создать окно GLFW")
        }

        GLFW.glfwMakeContextCurrent(window)
        GL.createCapabilities()

        this.window = window
    }

    fun loopaZalupa() {
        val program = Program.create {
            attach(
                Shader(
                    ShaderType.VERTEX,
                    ShaderSource(
                        """
                            #version 330 core

                            layout(location = 0) in vec3 Position;
                            layout(location = 1) in vec4 Color;

                            out vec4 color;

                            void main() {
                                gl_Position = vec4(Position, 1.0);
                                color = Color;
                            }
                        """.trimIndent()
                    )
                )
            )
            attach(
                Shader(
                    ShaderType.FRAGMENT,
                    ShaderSource(
                        """
                            #version 330 core

                            in vec4 color;
                            out vec4 fragColor;

                            void main() {
                                fragColor = color;
                            }
                        """.trimIndent()
                    )
                )
            )
            link()
            deleteShaders()
        }

        val mat = Matrix4f().identity().ortho(0f, 870f,870f, 0f, -100f,100f)
        val b = ValidatingBuilder(
            0,
            formatEbanat
        )

        fun Builder.quad(
            mat: Matrix4f,
            x: Float,
            y: Float,
            w: Float,
            h: Float,
            r: Float,
            g: Float,
            b: Float
        ) {
            pushFloat3(mat, Vector3f(x, y, 0f))
            pushFloat4(r,g,b, 1f)
            push()
            pushFloat3(mat, Vector3f(x+w, y, 0f))
            pushFloat4(r,g,b, 1f)
            push()
            pushFloat3(mat, Vector3f(x+w, y+h, 0f))
            pushFloat4(r,g,b, 1f)
            push()
            pushFloat3(mat, Vector3f(x, y+h, 0f))
            pushFloat4(r,g,b, 1f)
            push()

        }

        b.run {
            b.quad(
                mat,
                0f,0f,
                870f,870f/2,
                0f,0f,1f
            )
            b.quad(
                mat,
                0f,870f/2,
                870f,870f/2,
                1f,1f,0f
            )
            0
        }

        val mesh = Mesh(
            formatEbanat,
            DrawMode.QUADS,
            BufferUsage.STATIC_DRAW
        )
        mesh.upload(b)



        val fps = FpsController { 60.0 }
        while(!glfwWindowShouldClose(window)) {
            fps.update()

            glClearColor(0f, 0f, 0f, 1f)
            glClear(GL_COLOR_BUFFER_BIT)

            program.bind()
            mesh.bind()
            mesh.draw()

            glfwSwapBuffers(window)
            glfwPollEvents()

            fps.sync()
        }

        mesh.close()
        program.close()
        glfwTerminate()

        glfwSetErrorCallback(null)?.free()
    }
}

fun main() {
    DebugRunner.run(
        Taksa::init,
        Taksa::loopaZalupa,
    )
}
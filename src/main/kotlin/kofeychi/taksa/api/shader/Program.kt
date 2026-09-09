package kofeychi.taksa.api.shader

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.Closeable


class Program : Closeable {
    val id: Int = GL20.glCreateProgram()

    private val shaders = mutableListOf<Shader>()
    private val uniformLocationCache = mutableMapOf<String, Int>()

    init {
        if (id == 0) {
            throw ShaderException("Failed to create a valid shader program object.")
        }
    }

    fun attach(shader: Shader): Program {
        GL20.glAttachShader(id, shader.id)
        shaders.add(shader)
        return this
    }

    fun link(deleteShadersAfter: Boolean = true): Program {
        GL20.glLinkProgram(id)

        val status = GL20.glGetProgrami(id, GL20.GL_LINK_STATUS)
        if (status == GL11.GL_FALSE) {
            val infoLog = GL20.glGetProgramInfoLog(id)
            close()
            throw ShaderException("Failed to link shader program.\nInfo Log:\n$infoLog")
        }

        if (deleteShadersAfter) {
            shaders.forEach {
                GL20.glDetachShader(id, it.id)
                it.close()
            }
            shaders.clear()
        }

        return this
    }

    fun bind() {
        GL20.glUseProgram(id)
    }

    fun unbind() {
        GL20.glUseProgram(0)
    }

    override fun close() {
        unbind()
        if (id != 0) {
            GL20.glDeleteProgram(id)
        }
    }

    private fun getUniformLocation(name: String): Int {
        return uniformLocationCache.getOrPut(name) {
            val location = GL20.glGetUniformLocation(id, name)
            if (location == - 1) {
                println("Failed to get uniform location for $name")
            }
            location
        }
    }
}
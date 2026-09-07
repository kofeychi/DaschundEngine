package kofeychi.taksa.api.shader

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.Closeable

class Shader(
    val type: ShaderType,
    val source: ShaderSource
) : Closeable {
    val id: Int = GL20.glCreateShader(type.glEnum)

    init {
        if (id == 0) {
            throw ShaderException("Failed to create a valid shader object of type $type.")
        }

        compile()
    }

    private fun compile() {
        GL20.glShaderSource(id, source.source)
        GL20.glCompileShader(id)

        val status = GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS)
        if (status == GL11.GL_FALSE) {
            val infoLog = GL20.glGetShaderInfoLog(id)
            GL20.glDeleteShader(id)
            throw ShaderException("Failed to compile ${type.name} shader.\nInfo Log:\n$infoLog")
        }
    }

    override fun close() {
        if (id != 0) {
            GL20.glDeleteShader(id)
        }
    }
}
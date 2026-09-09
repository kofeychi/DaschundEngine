package kofeychi.taksa.api.shader

import kofeychi.taksa.api.ProgramId
import kofeychi.taksa.api.ShaderType
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.shader.uniform.Uniform
import java.io.Closeable

class Program : Closeable {
    val id = TypesafeGL.createProgram()

    companion object {
        fun create(action: Program.() -> Unit): Program {
            val program = Program()
            action(program)
            return program
        }
    }

    private val shaders = mutableMapOf<ShaderType, Shader>()
    private val uniforms = mutableMapOf<String, Uniform>()

    init {
        if(id.id == 0) throw ShaderException("Could not create program")
    }

    fun attach(shader: Shader) {
        if(shaders.containsKey(shader.type)) throw ShaderException("Shader '${shader.type}' already exists")
        TypesafeGL.attachShader(id,shader.id)
        shaders[shader.type] = shader
    }

    fun link() {
        TypesafeGL.linkProgram(id)

        if(!TypesafeGL.getProgramLinkStatus(id)) {
            val info = TypesafeGL.getProgramInfoLog(id)
            close()
            throw ShaderException("Failed to link shader program.\nInfo Log:\n$info")
        }
    }

    fun deleteShaders() {
        shaders.values.forEach {
            TypesafeGL.detachShader(id,it.id)
            it.close()
        }
        shaders.clear()
    }


    fun uniform(name: String): Uniform {
        require(name.isNotBlank()) { "Uniform name must not be blank." }
        return uniforms.getOrPut(name) { Uniform(this, name) }
    }

    fun useUniform(name: String,action: Uniform.() -> Unit) {
        uniform(name).action()
    }

    fun hasUniform(name: String): Boolean = TypesafeGL.getUniformLocation(id, name) >= 0

    fun clearUniformCache() {
        uniforms.clear()
    }

    fun bind() {
        TypesafeGL.useProgram(id)
    }

    fun unbind() {
        TypesafeGL.useProgram(ProgramId(0))
    }

    override fun close() {
        uniforms.clear()
        unbind()
        if (id.id == 0) return
        TypesafeGL.deleteProgram(id)
    }
}
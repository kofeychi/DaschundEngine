package kofeychi.taksa.api.shader

import kofeychi.taksa.api.AbstractResource
import kofeychi.taksa.api.ProgramId
import kofeychi.taksa.api.ShaderType
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.shader.uniform.Uniform
import java.util.concurrent.ConcurrentHashMap

class Program : AbstractResource() {
    val id: ProgramId
    private val attached = LinkedHashMap<ShaderType, Shader>()
    private val uniforms = ConcurrentHashMap<String, Uniform>()

    init {
        id = TypesafeGL.createProgram()
        check(id.id != 0) { "Could not create shader program." }
    }

    companion object {
        inline fun create(action: Program.() -> Unit): Program =
            Program().apply(action)

        fun linked(vararg shaders: Shader): Program =
            Program().apply {
                shaders.forEach(::attach)
                link()
            }
    }

    fun attach(shader: Shader): Program {
        check(!isClosed)
        check(attached[shader.type] == null) { "A ${shader.type.glEnum} shader is already attached." }
        check(!shader.isClosed)
        TypesafeGL.attachShader(id, shader.id)
        attached[shader.type] = shader
        return this
    }

    fun detach(shader: Shader): Program {
        if (attached.remove(shader.type) != null) {
            TypesafeGL.detachShader(id, shader.id)
        }
        return this
    }

    fun link(): Program {
        check(!isClosed)
        TypesafeGL.linkProgram(id)
        if (!TypesafeGL.getProgramLinkStatus(id)) {
            throw ShaderException.linker(TypesafeGL.getProgramInfoLog(id))
        }
        clearUniformCache()
        return this
    }

    fun deleteShaders() {
        val shaders = attached.values.toList()
        attached.clear()
        shaders.forEach { shader ->
            TypesafeGL.detachShader(id, shader.id)
            shader.close()
        }
    }

    fun bind(): Program {
        check(!isClosed)
        TypesafeGL.useProgram(id)
        return this
    }

    fun unbind() {
        if (!isClosed && TypesafeGL.state().program == id.id) TypesafeGL.useProgram(ProgramId(0))
    }

    fun uniform(name: String): Uniform {
        check(!isClosed)
        require(name.isNotBlank())
        return uniforms.getOrPut(name) { Uniform(this, name) }
    }

    fun hasUniform(name: String): Boolean = uniform(name).location >= 0

    fun clearUniformCache() {
        uniforms.values.forEach(Uniform::invalidate)
        uniforms.clear()
    }

    override fun onClose() {
        if (TypesafeGL.state().program == id.id) TypesafeGL.useProgram(ProgramId(0))
        attached.values.forEach { TypesafeGL.detachShader(id, it.id) }
        attached.clear()
        uniforms.clear()
        TypesafeGL.deleteProgram(id)
    }
}

package kofeychi.taksa.api.shader

import kofeychi.taksa.api.ProgramId
import kofeychi.taksa.api.ShaderId
import kofeychi.taksa.api.ShaderType
import kofeychi.taksa.api.TypesafeGL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
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


    fun bind() {
        TypesafeGL.useProgram(id)
    }

    fun unbind() {
        TypesafeGL.useProgram(ProgramId(0))
    }

    override fun close() {
        unbind()
        if (id.id == 0) return
        TypesafeGL.deleteProgram(id)
    }
}
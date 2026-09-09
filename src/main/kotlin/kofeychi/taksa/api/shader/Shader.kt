package kofeychi.taksa.api.shader

import kofeychi.taksa.api.ShaderType
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.VertexArrayId
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.io.Closeable

class Shader(
    val type: ShaderType,
    source: ShaderSource,
) : Closeable {
    val id = TypesafeGL.createShader(type)

    init {
        if(id.id == 0) throw ShaderException("Could not create shader of type $type")

        TypesafeGL.shaderSource(id,source.source)
        TypesafeGL.compileShader(id)

        if(!TypesafeGL.getShaderCompileStatus(id)) {
            val info = TypesafeGL.getShaderInfoLog(id)
            close()
            throw ShaderException("Failed to compile ${type} shader.\nInfo Log:\n$info")
        }
    }

    override fun close() {
        if(id.id == 0) return
        TypesafeGL.deleteShader(id)
    }
}
package kofeychi.taksa.api.shader

import kofeychi.taksa.api.ShaderType
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.util.AbstractResource

class Shader(
    val type: ShaderType,
    private val source: ShaderSource,
) : AbstractResource() {
    val id = TypesafeGL.createShader(type)

    init {
        if (id.id == 0) throw ShaderException("Could not create shader of type $type")

        TypesafeGL.shaderSource(id, source.source)
        TypesafeGL.compileShader(id)

        if (!TypesafeGL.getShaderCompileStatus(id)) {
            val info = TypesafeGL.getShaderInfoLog(id)
            close()
            throw ShaderException.fromCompileLog(type.toString(), source.source, info)
        }
    }

    override fun free() {
        if (id.id != 0) TypesafeGL.deleteShader(id)
    }
}

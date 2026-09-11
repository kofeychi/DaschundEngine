package kofeychi.taksa.api.shader

import kofeychi.taksa.api.AbstractResource
import kofeychi.taksa.api.ShaderId
import kofeychi.taksa.api.ShaderType
import kofeychi.taksa.api.TypesafeGL

class Shader(
    val type: ShaderType,
    val source: ShaderSource,
) : AbstractResource() {
    val id: ShaderId

    init {
        val created = TypesafeGL.createShader(type)
        check(created.id != 0) { "Could not create shader of type $type." }
        id = created
        try {
            TypesafeGL.shaderSource(id, source.source)
            TypesafeGL.compileShader(id)
            if (!TypesafeGL.getShaderCompileStatus(id)) {
                throw ShaderException.compiler(
                    typeName(type),
                    source.source,
                    TypesafeGL.getShaderInfoLog(id)
                )
            }
        } catch (t: Throwable) {
            TypesafeGL.deleteShader(id)
            throw t
        }
    }

    override fun onClose() {
        TypesafeGL.deleteShader(id)
    }

    private fun typeName(type: ShaderType) = when (type) {
        ShaderType.VERTEX -> "vertex"
        ShaderType.FRAGMENT -> "fragment"
        ShaderType.GEOMETRY -> "geometry"
        else -> "shader(${type.glEnum})"
    }
}

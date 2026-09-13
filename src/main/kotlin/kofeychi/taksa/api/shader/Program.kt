package kofeychi.taksa.api.shader

import kofeychi.taksa.api.ProgramId
import kofeychi.taksa.api.ShaderType
import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.shader.uniform.Uniform
import kofeychi.taksa.api.util.AbstractResource
import kofeychi.taksa.api.util.closeAll

class Program : AbstractResource() {
    val id = TypesafeGL.createProgram()

    companion object {
        /**
         * Builds, attaches, links and detaches shaders in one shot. On any failure
         * every shader created so far (and the program itself) is torn down before
         * the exception propagates, so a failed build never leaks GL handles.
         *
         * ```
         * val program = Program.link(
         *   vertex = ShaderSource(vertexSrc),
         *   fragment = ShaderSource(fragmentSrc),
         * )
         * ```
         */
        fun link(vertex: ShaderSource, fragment: ShaderSource): Program {
            val program = Program()
            try {
                program.attach(Shader(ShaderType.VERTEX, vertex))
                program.attach(Shader(ShaderType.FRAGMENT, fragment))
                program.link()
                program.deleteShaders()
                return program
            } catch (t: Throwable) {
                program.close()
                throw t
            }
        }

        fun create(action: Program.() -> Unit): Program {
            val program = Program()
            try {
                action(program)
                return program
            } catch (t: Throwable) {
                program.close()
                throw t
            }
        }
    }

    private val shaders = mutableMapOf<ShaderType, Shader>()
    private val uniforms = mutableMapOf<String, Uniform>()

    init {
        if (id.id == 0) throw ShaderException("Could not create program")
    }

    fun attach(shader: Shader) {
        checkOpen("program")
        if (shaders.containsKey(shader.type)) throw ShaderException("Shader '${shader.type}' already exists")
        TypesafeGL.attachShader(id, shader.id)
        shaders[shader.type] = shader
    }

    fun link() {
        checkOpen("program")
        TypesafeGL.linkProgram(id)

        if (!TypesafeGL.getProgramLinkStatus(id)) {
            val info = TypesafeGL.getProgramInfoLog(id)
            close()
            throw ShaderException.fromLinkLog(info)
        }
    }

    fun deleteShaders() {
        shaders.values.forEach {
            TypesafeGL.detachShader(id, it.id)
            it.close()
        }
        shaders.clear()
    }

    fun uniform(name: String): Uniform {
        require(name.isNotBlank()) { "Uniform name must not be blank." }
        return uniforms.getOrPut(name) { Uniform(this, name) }
    }

    fun useUniform(name: String, action: Uniform.() -> Unit) {
        uniform(name).action()
    }

    fun hasUniform(name: String): Boolean = TypesafeGL.getUniformLocation(id, name) >= 0

    fun clearUniformCache() {
        uniforms.values.forEach { it.invalidate() }
        uniforms.clear()
    }

    fun bind() {
        checkOpen("program")
        TypesafeGL.useProgram(id)
    }

    fun unbind() {
        TypesafeGL.useProgram(ProgramId(0))
    }

    inline fun <R> use(block: Program.() -> R): R {
        bind()
        try {
            return block()
        } finally {
            unbind()
        }
    }

    override fun free() {
        uniforms.clear()
        unbind()
        closeAll(shaders.values)
        shaders.clear()
        if (id.id != 0) TypesafeGL.deleteProgram(id)
    }
}

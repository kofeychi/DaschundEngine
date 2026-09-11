package kofeychi.taksa.api

import kofeychi.taksa.api.shader.uniform.Uniform
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryStack
import org.joml.Matrix2f
import org.joml.Matrix3f
import org.joml.Matrix4f
import java.nio.ByteBuffer

@JvmInline value class TextureId(val id: Int) {
    fun isValid() = id != 0
}
@JvmInline value class TextureTarget(val glEnum: Int) {
    companion object { val TEXTURE_2D = TextureTarget(GL11.GL_TEXTURE_2D) }
}
@JvmInline value class TextureFormat(val glEnum: Int) {
    companion object {
        val RED = TextureFormat(GL11.GL_RED)
        val RG = TextureFormat(GL30.GL_RG)
        val RGB = TextureFormat(GL11.GL_RGB)
        val RGBA = TextureFormat(GL11.GL_RGBA)
        val RED_INTEGER = TextureFormat(GL30.GL_RED_INTEGER)
        val RG_INTEGER = TextureFormat(GL30.GL_RG_INTEGER)
        val RGB_INTEGER = TextureFormat(GL30.GL_RGB_INTEGER)
        val RGBA_INTEGER = TextureFormat(GL30.GL_RGBA_INTEGER)
    }
}
@JvmInline value class TextureInternalFormat(val glEnum: Int) {
    companion object {
        val R8 = TextureInternalFormat(GL30.GL_R8)
        val RG8 = TextureInternalFormat(GL30.GL_RG8)
        val RGB8 = TextureInternalFormat(GL11.GL_RGB8)
        val RGBA8 = TextureInternalFormat(GL11.GL_RGBA8)
        val R16F = TextureInternalFormat(GL30.GL_R16F)
        val RG16F = TextureInternalFormat(GL30.GL_RG16F)
        val RGB16F = TextureInternalFormat(GL30.GL_RGB16F)
        val RGBA16F = TextureInternalFormat(GL30.GL_RGBA16F)
        val R32F = TextureInternalFormat(GL30.GL_R32F)
        val RG32F = TextureInternalFormat(GL30.GL_RG32F)
        val RGB32F = TextureInternalFormat(GL30.GL_RGB32F)
        val RGBA32F = TextureInternalFormat(GL30.GL_RGBA32F)
        val DEPTH24_STENCIL8 = TextureInternalFormat(GL30.GL_DEPTH24_STENCIL8)
    }
}
enum class TextureDataType(val glEnum: Int, val bytesPerComponent: Int) {
    UBYTE(GL11.GL_UNSIGNED_BYTE, 1), BYTE(GL11.GL_BYTE, 1),
    USHORT(GL11.GL_UNSIGNED_SHORT, 2), SHORT(GL11.GL_SHORT, 2),
    UINT(GL11.GL_UNSIGNED_INT, 4), INT(GL11.GL_INT, 4), FLOAT(GL11.GL_FLOAT, 4)
}
@JvmInline value class TextureWrap(val glEnum: Int) {
    companion object {
        val REPEAT = TextureWrap(GL11.GL_REPEAT)
        val MIRRORED_REPEAT = TextureWrap(GL14.GL_MIRRORED_REPEAT)
        val CLAMP_TO_EDGE = TextureWrap(GL12.GL_CLAMP_TO_EDGE)
        val CLAMP_TO_BORDER = TextureWrap(GL13.GL_CLAMP_TO_BORDER)
    }
}
@JvmInline value class TextureFilter(val glEnum: Int) {
    companion object {
        val NEAREST = TextureFilter(GL11.GL_NEAREST)
        val LINEAR = TextureFilter(GL11.GL_LINEAR)
        val NEAREST_MIPMAP_NEAREST = TextureFilter(GL11.GL_NEAREST_MIPMAP_NEAREST)
        val LINEAR_MIPMAP_NEAREST = TextureFilter(GL11.GL_LINEAR_MIPMAP_NEAREST)
        val NEAREST_MIPMAP_LINEAR = TextureFilter(GL11.GL_NEAREST_MIPMAP_LINEAR)
        val LINEAR_MIPMAP_LINEAR = TextureFilter(GL11.GL_LINEAR_MIPMAP_LINEAR)
    }
}
@JvmInline value class ProgramId(val id: Int)
@JvmInline value class ShaderId(val id: Int)
@JvmInline value class BufferId(val id: Int)
@JvmInline value class VertexArrayId(val id: Int)
@JvmInline value class FramebufferId(val id: Int)
@JvmInline value class RenderbufferId(val id: Int)

@JvmInline value class ShaderType(val glEnum: Int) {
    companion object {
        val VERTEX = ShaderType(GL20.GL_VERTEX_SHADER)
        val FRAGMENT = ShaderType(GL20.GL_FRAGMENT_SHADER)
        val GEOMETRY = ShaderType(GL32.GL_GEOMETRY_SHADER)
    }
}
@JvmInline value class BufferTarget(val glEnum: Int) {
    companion object {
        val ARRAY_BUFFER = BufferTarget(GL15.GL_ARRAY_BUFFER)
        val ELEMENT_ARRAY_BUFFER = BufferTarget(GL15.GL_ELEMENT_ARRAY_BUFFER)
        val UNIFORM_BUFFER = BufferTarget(GL31.GL_UNIFORM_BUFFER)
    }
}
@JvmInline value class BufferUsage(val glEnum: Int) {
    companion object {
        val STATIC_DRAW = BufferUsage(GL15.GL_STATIC_DRAW)
        val DYNAMIC_DRAW = BufferUsage(GL15.GL_DYNAMIC_DRAW)
        val STREAM_DRAW = BufferUsage(GL15.GL_STREAM_DRAW)
    }
}
@JvmInline value class DrawMode(val glEnum: Int) {
    companion object {
        val POINTS = DrawMode(GL11.GL_POINTS)
        val LINES = DrawMode(GL11.GL_LINES)
        val TRIANGLES = DrawMode(GL11.GL_TRIANGLES)
        val TRIANGLE_STRIP = DrawMode(GL11.GL_TRIANGLE_STRIP)
        val TRIANGLE_FAN = DrawMode(GL11.GL_TRIANGLE_FAN)
        @Deprecated("GL_QUADS is compatibility-profile only")
        val QUADS = DrawMode(GL11.GL_QUADS)
    }
}
@JvmInline value class GLDataType(val glEnum: Int) {
    companion object {
        val FLOAT = GLDataType(GL11.GL_FLOAT)
        val INT = GLDataType(GL11.GL_INT)
        val UNSIGNED_INT = GLDataType(GL11.GL_UNSIGNED_INT)
        val BYTE = GLDataType(GL11.GL_BYTE)
        val UNSIGNED_BYTE = GLDataType(GL11.GL_UNSIGNED_BYTE)
    }
}

class GLStateCache {
    var activeTextureUnit: Int = 0
        private set
    var program: Int = 0
        private set
    var vertexArray: Int = 0
        private set
    private val buffers = HashMap<Int, Int>()
    private val textures = HashMap<Long, Int>()

    fun activeTexture(unit: Int): Boolean {
        require(unit >= 0)
        if (activeTextureUnit == unit) return false
        activeTextureUnit = unit
        return true
    }

    fun bindProgram(id: Int): Boolean {
        if (program == id) return false
        program = id
        return true
    }

    fun bindVertexArray(id: Int): Boolean {
        if (vertexArray == id) return false
        vertexArray = id
        return true
    }

    fun bindBuffer(target: Int, id: Int): Boolean {
        if (buffers[target] == id) return false
        buffers[target] = id
        return true
    }

    fun bindTexture(unit: Int, target: Int, id: Int): Boolean {
        val key = (unit.toLong() shl 32) or (target.toLong() and 0xFFFF_FFFFL)
        if (textures[key] == id) return false
        textures[key] = id
        return true
    }

    fun invalidate() {
        activeTextureUnit = 0
        program = 0
        vertexArray = 0
        buffers.clear()
        textures.clear()
    }
}

object TypesafeGL {
    private val state = ThreadLocal.withInitial { GLStateCache() }
    fun state(): GLStateCache = state.get()

    fun invalidateState() = state.get().invalidate()

    fun createShader(type: ShaderType): ShaderId = ShaderId(GL20.glCreateShader(type.glEnum))
    fun shaderSource(shader: ShaderId, source: String) = GL20.glShaderSource(shader.id, source)
    fun compileShader(shader: ShaderId) = GL20.glCompileShader(shader.id)
    fun getShaderCompileStatus(shader: ShaderId): Boolean =
        GL20.glGetShaderi(shader.id, GL20.GL_COMPILE_STATUS) == GL11.GL_TRUE
    fun getShaderInfoLog(shader: ShaderId): String = GL20.glGetShaderInfoLog(shader.id)
    fun deleteShader(shader: ShaderId) { if (shader.id != 0) GL20.glDeleteShader(shader.id) }

    fun createProgram(): ProgramId = ProgramId(GL20.glCreateProgram())
    fun attachShader(program: ProgramId, shader: ShaderId) = GL20.glAttachShader(program.id, shader.id)
    fun detachShader(program: ProgramId, shader: ShaderId) = GL20.glDetachShader(program.id, shader.id)
    fun linkProgram(program: ProgramId) = GL20.glLinkProgram(program.id)
    fun getProgramLinkStatus(program: ProgramId): Boolean =
        GL20.glGetProgrami(program.id, GL20.GL_LINK_STATUS) == GL11.GL_TRUE
    fun getProgramInfoLog(program: ProgramId): String = GL20.glGetProgramInfoLog(program.id)
    fun useProgram(program: ProgramId) {
        if (state.get().bindProgram(program.id)) GL20.glUseProgram(program.id)
    }
    fun deleteProgram(program: ProgramId) {
        if (program.id != 0) {
            if (state.get().program == program.id) state.get().bindProgram(0)
            GL20.glDeleteProgram(program.id)
        }
    }

    fun getUniformLocation(program: ProgramId, name: String): Int = GL20.glGetUniformLocation(program.id, name)

    data class ActiveUniform(val name: String, val size: Int, val type: Int)

    fun getActiveUniformCount(program: ProgramId): Int = GL20.glGetProgrami(program.id, GL20.GL_ACTIVE_UNIFORMS)
    fun getActiveUniform(program: ProgramId, index: Int): ActiveUniform =
        MemoryStack.stackPush().use { stack ->
            val size = stack.mallocInt(1)
            val type = stack.mallocInt(1)
            val name = GL20.glGetActiveUniform(program.id, index, size, type)
            ActiveUniform(name, size[0], type[0])
        }

    fun getUniformType(program: ProgramId, name: String): Uniform.Type =
        findActiveUniform(program, name)?.let { Uniform.Type.from(it.type) } ?: Uniform.Type.UNKNOWN
    fun getUniformArrayLength(program: ProgramId, name: String): Int =
        findActiveUniform(program, name)?.size ?: 0

    private fun findActiveUniform(program: ProgramId, requestedName: String): ActiveUniform? {
        repeat(getActiveUniformCount(program)) {
            val u = getActiveUniform(program, it)
            if (u.name == requestedName || u.name.removeSuffix("[0]") == requestedName) return u
        }
        return null
    }

    fun uniform1f(location: Int, value: Float) = GL20.glUniform1f(location, value)
    fun uniform2f(location: Int, x: Float, y: Float) = GL20.glUniform2f(location, x, y)
    fun uniform3f(location: Int, x: Float, y: Float, z: Float) = GL20.glUniform3f(location, x, y, z)
    fun uniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) = GL20.glUniform4f(location, x, y, z, w)
    fun uniform1i(location: Int, value: Int) = GL20.glUniform1i(location, value)
    fun uniform2i(location: Int, x: Int, y: Int) = GL20.glUniform2i(location, x, y)
    fun uniform3i(location: Int, x: Int, y: Int, z: Int) = GL20.glUniform3i(location, x, y, z)
    fun uniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) = GL20.glUniform4i(location, x, y, z, w)
    fun uniform1fv(location: Int, values: FloatArray) = GL20.glUniform1fv(location, values)
    fun uniform1iv(location: Int, values: IntArray) = GL20.glUniform1iv(location, values)

    fun uniformMatrix2f(location: Int, transpose: Boolean, value: Matrix2f) =
        MemoryStack.stackPush().use { stack ->
            GL20.glUniformMatrix2fv(location, transpose, value.get(stack.mallocFloat(4)))
        }
    fun uniformMatrix3f(location: Int, transpose: Boolean, value: Matrix3f) =
        MemoryStack.stackPush().use { stack ->
            GL20.glUniformMatrix3fv(location, transpose, value.get(stack.mallocFloat(9)))
        }
    fun uniformMatrix4f(location: Int, transpose: Boolean, value: Matrix4f) =
        MemoryStack.stackPush().use { stack ->
            GL20.glUniformMatrix4fv(location, transpose, value.get(stack.mallocFloat(16)))
        }

    fun genVertexArrays(): VertexArrayId = VertexArrayId(GL30.glGenVertexArrays())
    fun bindVertexArray(vao: VertexArrayId) {
        if (state.get().bindVertexArray(vao.id)) GL30.glBindVertexArray(vao.id)
    }
    fun deleteVertexArrays(vao: VertexArrayId) {
        if (vao.id != 0) {
            if (state.get().vertexArray == vao.id) state.get().bindVertexArray(0)
            GL30.glDeleteVertexArrays(vao.id)
        }
    }

    fun genBuffers(): BufferId = BufferId(GL15.glGenBuffers())
    fun bindBuffer(target: BufferTarget, buffer: BufferId) {
        if (state.get().bindBuffer(target.glEnum, buffer.id)) GL15.glBindBuffer(target.glEnum, buffer.id)
    }
    fun bufferData(target: BufferTarget, data: ByteBuffer, usage: BufferUsage) =
        GL15.glBufferData(target.glEnum, data, usage.glEnum)
    fun bufferData(target: BufferTarget, size: Long, address: Long, usage: BufferUsage) =
        GL15.nglBufferData(target.glEnum, size, address, usage.glEnum)
    fun bufferSubData(target: BufferTarget, offset: Long, size: Long, address: Long) =
        GL15.nglBufferSubData(target.glEnum, offset, size, address)
    fun deleteBuffers(buffer: BufferId) { if (buffer.id != 0) GL15.glDeleteBuffers(buffer.id) }

    fun genTextures(): TextureId = TextureId(GL11.glGenTextures())
    fun activeTexture(unit: Int) {
        require(unit >= 0)
        if (state.get().activeTexture(unit)) GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit)
    }
    fun bindTexture(target: TextureTarget, texture: TextureId, unit: Int = state.get().activeTextureUnit) {
        require(unit >= 0)
        if (state.get().bindTexture(unit, target.glEnum, texture.id)) GL11.glBindTexture(target.glEnum, texture.id)
    }
    fun texParameteri(target: TextureTarget, parameter: Int, value: Int) =
        GL11.glTexParameteri(target.glEnum, parameter, value)
    fun texParameterf(target: TextureTarget, parameter: Int, value: Float) =
        GL11.glTexParameterf(target.glEnum, parameter, value)
    fun pixelStorei(parameter: Int, value: Int) = GL11.glPixelStorei(parameter, value)

    fun texImage2D(
        target: TextureTarget, level: Int, internalFormat: TextureInternalFormat,
        width: Int, height: Int, format: TextureFormat, dataType: TextureDataType,
        data: ByteBuffer?
    ) = GL11.glTexImage2D(target.glEnum, level, internalFormat.glEnum, width, height, 0, format.glEnum, dataType.glEnum, data)

    fun texSubImage2D(
        target: TextureTarget, level: Int, xOffset: Int, yOffset: Int, width: Int, height: Int,
        format: TextureFormat, dataType: TextureDataType, data: ByteBuffer
    ) = GL11.glTexSubImage2D(target.glEnum, level, xOffset, yOffset, width, height, format.glEnum, dataType.glEnum, data)

    fun generateMipmap(target: TextureTarget) = GL30.glGenerateMipmap(target.glEnum)
    fun deleteTextures(texture: TextureId) { if (texture.id != 0) GL11.glDeleteTextures(texture.id) }

    fun drawArrays(mode: DrawMode, first: Int, count: Int) = GL11.glDrawArrays(mode.glEnum, first, count)
    fun drawElements(mode: DrawMode, count: Int, type: GLDataType, offset: Long) =
        GL11.glDrawElements(mode.glEnum, count, type.glEnum, offset)

    fun enable(cap: Int) = GL11.glEnable(cap)
    fun disable(cap: Int) = GL11.glDisable(cap)
    fun scissor(x: Int, y: Int, width: Int, height: Int) = GL11.glScissor(x, y, width, height)
    fun viewport(x: Int, y: Int, width: Int, height: Int) = GL11.glViewport(x, y, width, height)
    fun clear(mask: Int) = GL11.glClear(mask)
    fun clearColor(r: Float, g: Float, b: Float, a: Float) = GL11.glClearColor(r, g, b, a)
    fun blendFunc(srcRgb: Int, dstRgb: Int, srcAlpha: Int, dstAlpha: Int) = GL14.glBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha)

    fun genFramebuffers(): FramebufferId = FramebufferId(GL30.glGenFramebuffers())
    fun bindFramebuffer(id: FramebufferId) = GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id.id)
    fun deleteFramebuffer(id: FramebufferId) { if (id.id != 0) GL30.glDeleteFramebuffers(id.id) }
    fun framebufferTexture2D(texture: TextureId) =
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture.id, 0)
    fun checkFramebufferComplete(): Boolean =
        GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE

    fun genRenderbuffers(): RenderbufferId = RenderbufferId(GL30.glGenRenderbuffers())
    fun bindRenderbuffer(id: RenderbufferId) = GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, id.id)
    fun renderbufferStorage(format: TextureInternalFormat, width: Int, height: Int) =
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, format.glEnum, width, height)
    fun framebufferRenderbuffer(attachment: Int, renderbuffer: RenderbufferId) =
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, attachment, GL30.GL_RENDERBUFFER, renderbuffer.id)
    fun deleteRenderbuffer(id: RenderbufferId) { if (id.id != 0) GL30.glDeleteRenderbuffers(id.id) }
}

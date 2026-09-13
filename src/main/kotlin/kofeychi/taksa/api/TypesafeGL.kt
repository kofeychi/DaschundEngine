package kofeychi.taksa.api

import kofeychi.taksa.api.shader.uniform.Uniform
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack

@JvmInline value class TextureId(val id: Int)

@JvmInline value class TextureTarget(val glEnum: Int) {
    companion object {
        val TEXTURE_2D = TextureTarget(GL11.GL_TEXTURE_2D)
    }
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
        val DEPTH_COMPONENT = TextureFormat(GL11.GL_DEPTH_COMPONENT)
    }
}

@JvmInline value class TextureInternalFormat(val glEnum: Int) {
    companion object {
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
        val DEPTH_COMPONENT24 = TextureInternalFormat(GL14.GL_DEPTH_COMPONENT24)
    }
}

enum class TextureDataType(val glEnum: Int, val bytesPerComponent: Int) {
    UBYTE(GL11.GL_UNSIGNED_BYTE, 1),
    BYTE(GL11.GL_BYTE, 1),
    USHORT(GL11.GL_UNSIGNED_SHORT, 2),
    SHORT(GL11.GL_SHORT, 2),
    UINT(GL11.GL_UNSIGNED_INT, 4),
    INT(GL11.GL_INT, 4),
    FLOAT(GL11.GL_FLOAT, 4)
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
    }
}

@JvmInline value class BufferTarget(val glEnum: Int) {
    companion object {
        val ARRAY_BUFFER = BufferTarget(GL15.GL_ARRAY_BUFFER)
        val ELEMENT_ARRAY_BUFFER = BufferTarget(GL15.GL_ELEMENT_ARRAY_BUFFER)
    }
}

@JvmInline value class BufferUsage(val glEnum: Int) {
    companion object {
        val STATIC_DRAW = BufferUsage(GL15.GL_STATIC_DRAW)
        val DYNAMIC_DRAW = BufferUsage(GL15.GL_DYNAMIC_DRAW)
        val STREAM_DRAW = BufferUsage(GL15.GL_STREAM_DRAW)
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

@JvmInline value class DrawMode(val glEnum: Int) {
    companion object {
        val POINTS = DrawMode(GL11.GL_POINTS)
        val LINES = DrawMode(GL11.GL_LINES)
        val LINE_STRIP = DrawMode(GL11.GL_LINE_STRIP)
        val TRIANGLES = DrawMode(GL11.GL_TRIANGLES)
        val TRIANGLE_STRIP = DrawMode(GL11.GL_TRIANGLE_STRIP)
        val TRIANGLE_FAN = DrawMode(GL11.GL_TRIANGLE_FAN)
    }
}

@JvmInline value class BlendMode private constructor(val src: Int, val dst: Int) {
    companion object {
        val NONE = BlendMode(-1, -1)
        val ALPHA = BlendMode(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val ADDITIVE = BlendMode(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
        val PREMULTIPLIED = BlendMode(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA)
    }

    val enabled: Boolean get() = this != NONE
}

/**
 * Thin, allocation-free wrapper over raw LWJGL GL calls.
 *
 * Two responsibilities live here:
 *  1. Typesafety: every GL enum/handle is a distinct inline value class so you can't
 *     accidentally pass a [BufferId] where a [TextureId] is expected.
 *  2. Redundant state elimination: binds/enables that would be no-ops against the
 *     currently-tracked GL state are skipped entirely. This is a huge win for layered
 *     rendering where many draw calls reuse the same program/texture/vao back to back.
 *
 * The cache is invalidated automatically whenever a handle is deleted so stale ids can
 * never be "seen" as still-bound.
 */
object TypesafeGL {

    // ---- tracked state -----------------------------------------------------
    private var boundProgram = ProgramId(0)
    private var boundVertexArray = VertexArrayId(0)
    private val boundBuffers = HashMap<Int, BufferId>()
    private var activeUnit = 0
    private val boundTextures = HashMap<Int, TextureId>() // key = texture unit
    private var boundFramebuffer = FramebufferId(0)
    private var blendMode = BlendMode.NONE
    private var scissorEnabled = false
    private var depthTestEnabled = false

    fun resetTrackedState() {
        boundProgram = ProgramId(0)
        boundVertexArray = VertexArrayId(0)
        boundBuffers.clear()
        boundTextures.clear()
        boundFramebuffer = FramebufferId(0)
        blendMode = BlendMode.NONE
        scissorEnabled = false
        depthTestEnabled = false
        activeUnit = 0
    }

    // ---- shaders -------------------------------------------------------------
    fun createShader(type: ShaderType): ShaderId = ShaderId(GL20.glCreateShader(type.glEnum))
    fun shaderSource(shader: ShaderId, source: String) = GL20.glShaderSource(shader.id, source)
    fun compileShader(shader: ShaderId) = GL20.glCompileShader(shader.id)
    fun getShaderCompileStatus(shader: ShaderId): Boolean =
        GL20.glGetShaderi(shader.id, GL20.GL_COMPILE_STATUS) == GL11.GL_TRUE
    fun getShaderInfoLog(shader: ShaderId): String = GL20.glGetShaderInfoLog(shader.id)
    fun deleteShader(shader: ShaderId) = GL20.glDeleteShader(shader.id)
    fun detachShader(program: ProgramId, shader: ShaderId) = GL20.glDetachShader(program.id, shader.id)

    // ---- programs --------------------------------------------------------------
    fun createProgram(): ProgramId = ProgramId(GL20.glCreateProgram())
    fun attachShader(program: ProgramId, shader: ShaderId) = GL20.glAttachShader(program.id, shader.id)
    fun linkProgram(program: ProgramId) = GL20.glLinkProgram(program.id)
    fun getProgramLinkStatus(program: ProgramId): Boolean =
        GL20.glGetProgrami(program.id, GL20.GL_LINK_STATUS) == GL11.GL_TRUE
    fun getProgramInfoLog(program: ProgramId): String = GL20.glGetProgramInfoLog(program.id)

    fun useProgram(program: ProgramId) {
        if (boundProgram == program) return
        GL20.glUseProgram(program.id)
        boundProgram = program
    }

    fun deleteProgram(program: ProgramId) {
        if (boundProgram == program) boundProgram = ProgramId(0)
        GL20.glDeleteProgram(program.id)
    }

    // ---- uniforms ----------------------------------------------------------------
    fun getUniformLocation(program: ProgramId, name: String): Int = GL20.glGetUniformLocation(program.id, name)
    fun getActiveUniformCount(program: ProgramId): Int = GL20.glGetProgrami(program.id, GL20.GL_ACTIVE_UNIFORMS)

    fun getActiveUniform(program: ProgramId, index: Int): ActiveUniform {
        MemoryStack.stackPush().use { stack ->
            val size = stack.mallocInt(1)
            val type = stack.mallocInt(1)
            val name = GL20.glGetActiveUniform(program.id, index, size, type)
            return ActiveUniform(name, size.get(0), type.get(0))
        }
    }

    fun getUniformType(program: ProgramId, name: String): Uniform.Type {
        val uniform = findActiveUniform(program, name)
        return Uniform.Type.from(uniform?.type ?: -1)
    }

    fun getUniformArrayLength(program: ProgramId, name: String): Int =
        findActiveUniform(program, name)?.size ?: 0

    private fun findActiveUniform(program: ProgramId, requestedName: String): ActiveUniform? {
        repeat(getActiveUniformCount(program)) { index ->
            val uniform = getActiveUniform(program, index)
            if (uniform.name == requestedName || uniform.name.removeSuffix("[0]") == requestedName) {
                return uniform
            }
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

    fun uniformMatrix2f(location: Int, transpose: Boolean, value: org.joml.Matrix2f) {
        MemoryStack.stackPush().use { stack -> GL20.glUniformMatrix2fv(location, transpose, value.get(stack.mallocFloat(4))) }
    }

    fun uniformMatrix3f(location: Int, transpose: Boolean, value: org.joml.Matrix3f) {
        MemoryStack.stackPush().use { stack -> GL20.glUniformMatrix3fv(location, transpose, value.get(stack.mallocFloat(9))) }
    }

    fun uniformMatrix4f(location: Int, transpose: Boolean, value: org.joml.Matrix4f) {
        MemoryStack.stackPush().use { stack -> GL20.glUniformMatrix4fv(location, transpose, value.get(stack.mallocFloat(16))) }
    }

    data class ActiveUniform(val name: String, val size: Int, val type: Int)

    // ---- vertex arrays -----------------------------------------------------------
    fun genVertexArrays(): VertexArrayId = VertexArrayId(GL30.glGenVertexArrays())

    fun bindVertexArray(vao: VertexArrayId) {
        if (boundVertexArray == vao) return
        GL30.glBindVertexArray(vao.id)
        boundVertexArray = vao
    }

    fun deleteVertexArrays(vao: VertexArrayId) {
        if (boundVertexArray == vao) boundVertexArray = VertexArrayId(0)
        GL30.glDeleteVertexArrays(vao.id)
    }

    // ---- buffers -------------------------------------------------------------------
    fun genBuffers(): BufferId = BufferId(GL15.glGenBuffers())

    fun bindBuffer(target: BufferTarget, buffer: BufferId) {
        if (boundBuffers[target.glEnum] == buffer) return
        GL15.glBindBuffer(target.glEnum, buffer.id)
        boundBuffers[target.glEnum] = buffer
    }

    fun nBufferData(target: BufferTarget, size: Long, data: Long, usage: BufferUsage) =
        GL15.nglBufferData(target.glEnum, size, data, usage.glEnum)

    fun nBufferSubData(target: BufferTarget, offset: Long, size: Long, data: Long) =
        GL15.nglBufferSubData(target.glEnum, offset, size, data)

    fun deleteBuffers(buffer: BufferId) {
        boundBuffers.entries.removeAll { it.value == buffer }
        GL15.glDeleteBuffers(buffer.id)
    }

    // ---- textures -----------------------------------------------------------------
    fun genTextures(): TextureId = TextureId(GL11.glGenTextures())

    fun activeTexture(unit: Int) {
        if (activeUnit == unit) return
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit)
        activeUnit = unit
    }

    fun bindTexture(target: TextureTarget, texture: TextureId, unit: Int = activeUnit) {
        activeTexture(unit)
        if (boundTextures[unit] == texture) return
        GL11.glBindTexture(target.glEnum, texture.id)
        boundTextures[unit] = texture
    }

    fun texParameteri(target: TextureTarget, parameter: Int, value: Int) = GL11.glTexParameteri(target.glEnum, parameter, value)
    fun pixelStorei(parameter: Int, value: Int) = GL11.glPixelStorei(parameter, value)
    fun texParameterf(target: TextureTarget, parameter: Int, value: Float) = GL11.glTexParameterf(target.glEnum, parameter, value)

    fun texImage2D(
        target: TextureTarget, level: Int, internalFormat: TextureInternalFormat,
        width: Int, height: Int, format: TextureFormat, dataType: TextureDataType, data: java.nio.ByteBuffer?,
    ) = GL11.glTexImage2D(target.glEnum, level, internalFormat.glEnum, width, height, 0, format.glEnum, dataType.glEnum, data)

    fun texSubImage2D(
        target: TextureTarget, level: Int, xOffset: Int, yOffset: Int,
        width: Int, height: Int, format: TextureFormat, dataType: TextureDataType, data: java.nio.ByteBuffer,
    ) = GL11.glTexSubImage2D(target.glEnum, level, xOffset, yOffset, width, height, format.glEnum, dataType.glEnum, data)

    fun generateMipmap(target: TextureTarget) = GL30.glGenerateMipmap(target.glEnum)

    fun deleteTextures(texture: TextureId) {
        boundTextures.entries.removeAll { it.value == texture }
        GL11.glDeleteTextures(texture.id)
    }

    // ---- framebuffers (offscreen render targets) -----------------------------------
    fun genFramebuffers(): FramebufferId = FramebufferId(GL30.glGenFramebuffers())

    fun bindFramebuffer(fbo: FramebufferId) {
        if (boundFramebuffer == fbo) return
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.id)
        boundFramebuffer = fbo
    }

    fun framebufferTexture2D(attachment: Int, target: TextureTarget, texture: TextureId, level: Int = 0) =
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, target.glEnum, texture.id, level)

    fun checkFramebufferStatus(): Int = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)

    fun deleteFramebuffers(fbo: FramebufferId) {
        if (boundFramebuffer == fbo) boundFramebuffer = FramebufferId(0)
        GL30.glDeleteFramebuffers(fbo.id)
    }

    fun genRenderbuffers(): RenderbufferId = RenderbufferId(GL30.glGenRenderbuffers())
    fun bindRenderbuffer(rbo: RenderbufferId) = GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo.id)
    fun renderbufferStorage(internalFormat: TextureInternalFormat, width: Int, height: Int) =
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, internalFormat.glEnum, width, height)
    fun framebufferRenderbuffer(attachment: Int, rbo: RenderbufferId) =
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, attachment, GL30.GL_RENDERBUFFER, rbo.id)
    fun deleteRenderbuffers(rbo: RenderbufferId) = GL30.glDeleteRenderbuffers(rbo.id)

    // ---- pipeline state ------------------------------------------------------------
    fun viewport(x: Int, y: Int, width: Int, height: Int) = GL11.glViewport(x, y, width, height)

    fun clearColor(r: Float, g: Float, b: Float, a: Float) = GL11.glClearColor(r, g, b, a)

    fun clear(color: Boolean = true, depth: Boolean = false, stencil: Boolean = false) {
        var mask = 0
        if (color) mask = mask or GL11.GL_COLOR_BUFFER_BIT
        if (depth) mask = mask or GL11.GL_DEPTH_BUFFER_BIT
        if (stencil) mask = mask or GL11.GL_STENCIL_BUFFER_BIT
        if (mask != 0) GL11.glClear(mask)
    }

    fun setBlendMode(mode: BlendMode) {
        if (blendMode == mode) return
        if (mode.enabled) {
            if (!blendMode.enabled) GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(mode.src, mode.dst)
        } else {
            GL11.glDisable(GL11.GL_BLEND)
        }
        blendMode = mode
    }

    fun setDepthTest(enabled: Boolean) {
        if (depthTestEnabled == enabled) return
        if (enabled) GL11.glEnable(GL11.GL_DEPTH_TEST) else GL11.glDisable(GL11.GL_DEPTH_TEST)
        depthTestEnabled = enabled
    }

    fun setScissor(x: Int, y: Int, width: Int, height: Int) {
        if (!scissorEnabled) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            scissorEnabled = true
        }
        GL11.glScissor(x, y, width, height)
    }

    fun clearScissor() {
        if (!scissorEnabled) return
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        scissorEnabled = false
    }

    fun drawArrays(mode: DrawMode, first: Int, count: Int) {
        if (count <= 0) return
        GL11.glDrawArrays(mode.glEnum, first, count)
    }
}

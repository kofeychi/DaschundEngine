package kofeychi.taksa.api

import kofeychi.taksa.api.shader.uniform.Uniform
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14


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
        val TRIANGLES = DrawMode(GL15.GL_TRIANGLES)
        val QUADS = DrawMode(GL15.GL_QUADS)
    }
}

object TypesafeGL {

    fun createShader(type: ShaderType): ShaderId {
        return ShaderId(GL20.glCreateShader(type.glEnum))
    }

    fun shaderSource(shader: ShaderId, source: String) {
        GL20.glShaderSource(shader.id, source)
    }

    fun compileShader(shader: ShaderId) {
        GL20.glCompileShader(shader.id)
    }

    fun getShaderCompileStatus(shader: ShaderId): Boolean {
        return GL20.glGetShaderi(shader.id, GL20.GL_COMPILE_STATUS) == GL11.GL_TRUE
    }

    fun getShaderInfoLog(shader: ShaderId): String {
        return GL20.glGetShaderInfoLog(shader.id)
    }

    fun deleteShader(shader: ShaderId) {
        GL20.glDeleteShader(shader.id)
    }

    fun detachShader(program: ProgramId, shader: ShaderId) {
        GL20.glDetachShader(program.id,shader.id)
    }



    fun createProgram(): ProgramId {
        return ProgramId(GL20.glCreateProgram())
    }

    fun attachShader(program: ProgramId, shader: ShaderId) {
        GL20.glAttachShader(program.id, shader.id)
    }

    fun linkProgram(program: ProgramId) {
        GL20.glLinkProgram(program.id)
    }

    fun getProgramLinkStatus(program: ProgramId): Boolean {
        return GL20.glGetProgrami(program.id, GL20.GL_LINK_STATUS) == GL11.GL_TRUE
    }

    fun getProgramInfoLog(program: ProgramId): String {
        return GL20.glGetProgramInfoLog(program.id)
    }

    fun useProgram(program: ProgramId) {
        GL20.glUseProgram(program.id)
    }

    fun deleteProgram(program: ProgramId) {
        GL20.glDeleteProgram(program.id)
    }



    fun getUniformLocation(program: ProgramId, name: String): Int {
        return GL20.glGetUniformLocation(program.id, name)
    }

    fun getActiveUniformCount(program: ProgramId): Int {
        return GL20.glGetProgrami(program.id, GL20.GL_ACTIVE_UNIFORMS)
    }

    fun getActiveUniform(program: ProgramId, index: Int): ActiveUniform {
        org.lwjgl.system.MemoryStack.stackPush().use { stack ->
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

    fun getUniformArrayLength(program: ProgramId, name: String): Int {
        return findActiveUniform(program, name)?.size ?: 0
    }

    private fun findActiveUniform(program: ProgramId, requestedName: String): ActiveUniform? {
        repeat(getActiveUniformCount(program)) { index ->
            val uniform = getActiveUniform(program, index)
            if (uniform.name == requestedName || uniform.name.removeSuffix("[0]") == requestedName) {
                return uniform
            }
        }
        return null
    }

    fun uniform1f(program: ProgramId, location: Int, value: Float) { GL20.glUniform1f(location, value) }
    fun uniform2f(program: ProgramId, location: Int, x: Float, y: Float) { GL20.glUniform2f(location, x, y) }
    fun uniform3f(program: ProgramId, location: Int, x: Float, y: Float, z: Float) { GL20.glUniform3f(location, x, y, z) }
    fun uniform4f(program: ProgramId, location: Int, x: Float, y: Float, z: Float, w: Float) { GL20.glUniform4f(location, x, y, z, w) }
    fun uniform1i(program: ProgramId, location: Int, value: Int) { GL20.glUniform1i(location, value) }
    fun uniform2i(program: ProgramId, location: Int, x: Int, y: Int) { GL20.glUniform2i(location, x, y) }
    fun uniform3i(program: ProgramId, location: Int, x: Int, y: Int, z: Int) { GL20.glUniform3i(location, x, y, z) }
    fun uniform4i(program: ProgramId, location: Int, x: Int, y: Int, z: Int, w: Int) { GL20.glUniform4i(location, x, y, z, w) }

    fun uniform1fv(program: ProgramId, location: Int, values: FloatArray) { GL20.glUniform1fv(location, values) }
    fun uniform1iv(program: ProgramId, location: Int, values: IntArray) { GL20.glUniform1iv(location, values) }

    fun uniformMatrix2f(program: ProgramId, location: Int, transpose: Boolean, value: org.joml.Matrix2f) {
        org.lwjgl.system.MemoryStack.stackPush().use { stack ->
            GL20.glUniformMatrix2fv(location, transpose, value.get(stack.mallocFloat(4)))
        }
    }

    fun uniformMatrix3f(program: ProgramId, location: Int, transpose: Boolean, value: org.joml.Matrix3f) {
        org.lwjgl.system.MemoryStack.stackPush().use { stack ->
            GL20.glUniformMatrix3fv(location, transpose, value.get(stack.mallocFloat(9)))
        }
    }

    fun uniformMatrix4f(program: ProgramId, location: Int, transpose: Boolean, value: org.joml.Matrix4f) {
        org.lwjgl.system.MemoryStack.stackPush().use { stack ->
            GL20.glUniformMatrix4fv(location, transpose, value.get(stack.mallocFloat(16)))
        }
    }

    data class ActiveUniform(
        val name: String,
        val size: Int,
        val type: Int,
    )

    fun genVertexArrays(): VertexArrayId {
        return VertexArrayId(GL30.glGenVertexArrays())
    }

    fun bindVertexArray(vao: VertexArrayId) {
        GL30.glBindVertexArray(vao.id)
    }

    fun deleteVertexArrays(vao: VertexArrayId) {
        GL30.glDeleteVertexArrays(vao.id)

    }



    fun genBuffers(): BufferId {
        return BufferId(GL15.glGenBuffers())
    }

    fun bindBuffer(target: BufferTarget, buffer: BufferId) {
        GL15.glBindBuffer(target.glEnum, buffer.id)
    }

    fun nBufferData(target: BufferTarget, size: Long, data: Long, usage: BufferUsage) {
        GL15.nglBufferData(target.glEnum, size, data, usage.glEnum)
    }

    fun nBufferSubData(target: BufferTarget, offset: Long, size: Long, data: Long) {
        GL15.nglBufferSubData(target.glEnum, offset, size, data)
    }

    fun deleteBuffers(buffer: BufferId) {
        GL15.glDeleteBuffers(buffer.id)
    }



    fun genTextures(): TextureId {
        return TextureId(GL11.glGenTextures())
    }

    fun activeTexture(unit: Int) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit)
    }

    fun bindTexture(target: TextureTarget, texture: TextureId) {
        GL11.glBindTexture(target.glEnum, texture.id)
    }

    fun texParameteri(target: TextureTarget, parameter: Int, value: Int) {
        GL11.glTexParameteri(target.glEnum, parameter, value)
    }

    fun pixelStorei(parameter: Int, value: Int) {
        GL11.glPixelStorei(parameter, value)
    }

    fun texParameterf(target: TextureTarget, parameter: Int, value: Float) {
        GL11.glTexParameterf(target.glEnum, parameter, value)
    }

    fun texImage2D(
        target: TextureTarget,
        level: Int,
        internalFormat: TextureInternalFormat,
        width: Int,
        height: Int,
        format: TextureFormat,
        dataType: TextureDataType,
        data: java.nio.ByteBuffer?,
    ) {
        GL11.glTexImage2D(
            target.glEnum,
            level,
            internalFormat.glEnum,
            width,
            height,
            0,
            format.glEnum,
            dataType.glEnum,
            data,
        )
    }

    fun texSubImage2D(
        target: TextureTarget,
        level: Int,
        xOffset: Int,
        yOffset: Int,
        width: Int,
        height: Int,
        format: TextureFormat,
        dataType: TextureDataType,
        data: java.nio.ByteBuffer,
    ) {
        GL11.glTexSubImage2D(
            target.glEnum,
            level,
            xOffset,
            yOffset,
            width,
            height,
            format.glEnum,
            dataType.glEnum,
            data,
        )
    }

    fun generateMipmap(target: TextureTarget) {
        GL30.glGenerateMipmap(target.glEnum)
    }

    fun deleteTextures(texture: TextureId) {
        GL11.glDeleteTextures(texture.id)
    }


    fun drawArrays(mode: DrawMode,first: Int, count: Int) {
        GL15.glDrawArrays(mode.glEnum, first, count)
    }
}
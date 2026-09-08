package kofeychi.taksa.api

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

@JvmInline value class ProgramId(val id: Int)

@JvmInline value class ShaderId(val id: Int)

@JvmInline value class BufferId(val id: Int)

@JvmInline value class VertexArrayId(val id: Int)

@JvmInline value class ShaderType(val glEnum: Int) {
    companion object {
        val VERTEX_SHADER = ShaderType(GL20.GL_VERTEX_SHADER)
        val FRAGMENT_SHADER = ShaderType(GL20.GL_FRAGMENT_SHADER)
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


    fun drawArrays(mode: DrawMode,first: Int, count: Int) {
        GL15.glDrawArrays(mode.glEnum, first, count)
    }
}
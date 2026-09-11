package kofeychi.taksa.api.shader.uniform

import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.shader.Program
import org.joml.Matrix2f
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.joml.Vector4fc
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack

@JvmInline
value class Color(val rgba: Int) {
    val r get() = ((rgba ushr 24) and 0xFF) / 255f
    val g get() = ((rgba ushr 16) and 0xFF) / 255f
    val b get() = ((rgba ushr 8) and 0xFF) / 255f
    val a get() = (rgba and 0xFF) / 255f
}

class Uniform internal constructor(
    private val program: Program,
    val name: String,
) {
    private var locationCache: Int? = null
    private var typeCache: Type? = null
    private var arrayLengthCache: Int? = null

    val location: Int
        get() = locationCache ?: TypesafeGL.getUniformLocation(program.id, name).also { locationCache = it }

    val type: Type
        get() = typeCache ?: TypesafeGL.getUniformType(program.id, name).also { typeCache = it }

    val arrayLength: Int
        get() = arrayLengthCache ?: TypesafeGL.getUniformArrayLength(program.id, name).also { arrayLengthCache = it }

    fun invalidate() {
        locationCache = null
        typeCache = null
        arrayLengthCache = null
    }

    private inline fun checked(block: () -> Unit): Uniform {
        check(location >= 0) { "Uniform '$name' is not active in program ${program.id.id}." }
        block()
        return this
    }

    fun set(value: Float) = checked { TypesafeGL.uniform1f(location, value) }
    fun set(value: Int) = checked { TypesafeGL.uniform1i(location, value) }
    fun set(value: UInt) = checked { TypesafeGL.uniform1i(location, value.toInt()) }
    fun set(value: Boolean) = set(if (value) 1 else 0)

    fun set(x: Float, y: Float) = checked { TypesafeGL.uniform2f(location, x, y) }
    fun set(x: Float, y: Float, z: Float) = checked { TypesafeGL.uniform3f(location, x, y, z) }
    fun set(x: Float, y: Float, z: Float, w: Float) = checked { TypesafeGL.uniform4f(location, x, y, z, w) }

    fun set(x: Int, y: Int) = checked { TypesafeGL.uniform2i(location, x, y) }
    fun set(x: Int, y: Int, z: Int) = checked { TypesafeGL.uniform3i(location, x, y, z) }
    fun set(x: Int, y: Int, z: Int, w: Int) = checked { TypesafeGL.uniform4i(location, x, y, z, w) }

    fun set(value: Vector2fc) = set(value.x(), value.y())
    fun set(value: Vector3fc) = set(value.x(), value.y(), value.z())
    fun set(value: Vector4fc) = set(value.x(), value.y(), value.z(), value.w())
    fun set(value: Color) = set(value.r, value.g, value.b, value.a)

    fun set(value: Matrix2f, transpose: Boolean = false) =
        checked { TypesafeGL.uniformMatrix2f(location, transpose, value) }

    fun set(value: Matrix3f, transpose: Boolean = false) =
        checked { TypesafeGL.uniformMatrix3f(location, transpose, value) }

    fun set(value: Matrix4f, transpose: Boolean = false) =
        checked { TypesafeGL.uniformMatrix4f(location, transpose, value) }

    fun set(values: FloatArray) = checked { TypesafeGL.uniform1fv(location, values) }
    fun set(values: IntArray) = checked { TypesafeGL.uniform1iv(location, values) }

    fun set(values: Array<Vector2fc>) = checked {
        MemoryStack.stackPush().use { stack ->
            val data = stack.mallocFloat(values.size * 2)
            values.forEach { it.get(data) }
            data.flip()
            GL20.glUniform2fv(location, data)
        }
    }

    fun set(values: Array<Vector3fc>) = checked {
        MemoryStack.stackPush().use { stack ->
            val data = stack.mallocFloat(values.size * 3)
            values.forEach { it.get(data) }
            data.flip()
            GL20.glUniform3fv(location, data)
        }
    }

    fun setTextureUnit(unit: Int): Uniform = set(unit)

    enum class Type(val glEnum: Int) {
        FLOAT(GL20.GL_FLOAT), FLOAT_VEC2(GL20.GL_FLOAT_VEC2), FLOAT_VEC3(GL20.GL_FLOAT_VEC3),
        FLOAT_VEC4(GL20.GL_FLOAT_VEC4), INT(GL20.GL_INT), INT_VEC2(GL20.GL_INT_VEC2),
        INT_VEC3(GL20.GL_INT_VEC3), INT_VEC4(GL20.GL_INT_VEC4),
        UNSIGNED_INT(GL30.GL_UNSIGNED_INT), UNSIGNED_INT_VEC2(GL30.GL_UNSIGNED_INT_VEC2),
        UNSIGNED_INT_VEC3(GL30.GL_UNSIGNED_INT_VEC3), UNSIGNED_INT_VEC4(GL30.GL_UNSIGNED_INT_VEC4),
        FLOAT_MAT2(GL20.GL_FLOAT_MAT2), FLOAT_MAT3(GL20.GL_FLOAT_MAT3), FLOAT_MAT4(GL20.GL_FLOAT_MAT4),
        SAMPLER_2D(GL20.GL_SAMPLER_2D), SAMPLER_CUBE(GL20.GL_SAMPLER_CUBE), UNKNOWN(-1);

        companion object {
            fun from(glEnum: Int): Type = values().firstOrNull { it.glEnum == glEnum } ?: UNKNOWN
        }
    }
}

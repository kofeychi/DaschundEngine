package kofeychi.taksa.api.shader.uniform

import kofeychi.taksa.api.TypesafeGL
import kofeychi.taksa.api.shader.Program
import kofeychi.taksa.api.texture.Texture
import org.joml.Matrix2f
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30

class Uniform internal constructor(
    private val program: Program,
    val name: String,
) {
    private var cachedLocation: Int? = null
    private var cachedType: Type? = null
    private var cachedArrayLength: Int? = null

    /** glGetUniformLocation is only ever called once per (program, name); every subsequent read hits this cache. */
    val location: Int
        get() = cachedLocation ?: TypesafeGL.getUniformLocation(program.id, name).also { cachedLocation = it }

    val type: Type
        get() = cachedType ?: TypesafeGL.getUniformType(program.id, name).also { cachedType = it }

    val arrayLength: Int
        get() = cachedArrayLength ?: TypesafeGL.getUniformArrayLength(program.id, name).also { cachedArrayLength = it }

    val exists: Boolean get() = location >= 0

    fun invalidate() {
        cachedLocation = null
        cachedType = null
        cachedArrayLength = null
    }

    fun set(value: Float) = apply { TypesafeGL.uniform1f(location, value) }
    fun set(value: Int) = apply { TypesafeGL.uniform1i(location, value) }
    fun set(value: Boolean) = set(if (value) 1 else 0)

    fun set(x: Float, y: Float) = apply { TypesafeGL.uniform2f(location, x, y) }
    fun set(x: Float, y: Float, z: Float) = apply { TypesafeGL.uniform3f(location, x, y, z) }
    fun set(x: Float, y: Float, z: Float, w: Float) = apply { TypesafeGL.uniform4f(location, x, y, z, w) }

    fun set(x: Int, y: Int) = apply { TypesafeGL.uniform2i(location, x, y) }
    fun set(x: Int, y: Int, z: Int) = apply { TypesafeGL.uniform3i(location, x, y, z) }
    fun set(x: Int, y: Int, z: Int, w: Int) = apply { TypesafeGL.uniform4i(location, x, y, z, w) }

    fun set(value: Vector2f) = set(value.x, value.y)
    fun set(value: Vector3f) = set(value.x, value.y, value.z)
    fun set(value: Vector4f) = set(value.x, value.y, value.z, value.w)

    /** Interprets an 0xAARRGGBB packed int as a normalized vec4, handy for `fill(color)`-style uniforms. */
    fun setColor(argb: Int) = set(
        ((argb ushr 16) and 0xFF) / 255f,
        ((argb ushr 8) and 0xFF) / 255f,
        (argb and 0xFF) / 255f,
        ((argb ushr 24) and 0xFF) / 255f,
    )

    fun set(value: Matrix2f, transpose: Boolean = false) = apply { TypesafeGL.uniformMatrix2f(location, transpose, value) }
    fun set(value: Matrix3f, transpose: Boolean = false) = apply { TypesafeGL.uniformMatrix3f(location, transpose, value) }
    fun set(value: Matrix4f, transpose: Boolean = false) = apply { TypesafeGL.uniformMatrix4f(location, transpose, value) }
    fun set(values: FloatArray) = apply { TypesafeGL.uniform1fv(location, values) }
    fun set(values: IntArray) = apply { TypesafeGL.uniform1iv(location, values) }

    /** Binds [texture] to [unit] and points this sampler uniform at it. Order-independent w.r.t. program.bind(). */
    fun set(texture: Texture, unit: Int = 0) = apply {
        texture.bind(unit)
        TypesafeGL.uniform1i(location, unit)
    }

    private inline fun apply(block: () -> Unit): Uniform {
        check(location >= 0) { "Uniform '$name' does not exist in program ${program.id.id} (or was optimized out)." }
        block()
        return this
    }

    enum class Type(val glEnum: Int) {
        FLOAT(GL20.GL_FLOAT),
        FLOAT_VEC2(GL20.GL_FLOAT_VEC2),
        FLOAT_VEC3(GL20.GL_FLOAT_VEC3),
        FLOAT_VEC4(GL20.GL_FLOAT_VEC4),
        INT(GL20.GL_INT),
        INT_VEC2(GL20.GL_INT_VEC2),
        INT_VEC3(GL20.GL_INT_VEC3),
        INT_VEC4(GL20.GL_INT_VEC4),
        UNSIGNED_INT(GL30.GL_UNSIGNED_INT),
        UNSIGNED_INT_VEC2(GL30.GL_UNSIGNED_INT_VEC2),
        UNSIGNED_INT_VEC3(GL30.GL_UNSIGNED_INT_VEC3),
        UNSIGNED_INT_VEC4(GL30.GL_UNSIGNED_INT_VEC4),
        FLOAT_MAT2(GL20.GL_FLOAT_MAT2),
        FLOAT_MAT3(GL20.GL_FLOAT_MAT3),
        FLOAT_MAT4(GL20.GL_FLOAT_MAT4),
        SAMPLER_2D(GL20.GL_SAMPLER_2D),
        SAMPLER_CUBE(GL20.GL_SAMPLER_CUBE),
        UNKNOWN(-1),
        ;

        companion object {
            fun from(glEnum: Int): Type = values().firstOrNull { it.glEnum == glEnum } ?: UNKNOWN
        }
    }
}

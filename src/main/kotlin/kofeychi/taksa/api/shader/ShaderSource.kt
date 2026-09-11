package kofeychi.taksa.api.shader

@JvmInline
value class ShaderSource(val source: String) {
    init { require(source.isNotBlank()) { "Shader source must not be blank." } }

    companion object {
        fun of(source: String) = ShaderSource(source)
        fun numbered(source: String): String =
            source.lineSequence().mapIndexed { index, line -> "%4d | %s".format(index + 1, line) }.joinToString("\n")
    }
}

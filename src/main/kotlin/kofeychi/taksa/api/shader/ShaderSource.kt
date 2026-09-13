package kofeychi.taksa.api.shader

@JvmInline value class ShaderSource(val source: String) {
    companion object {
        fun of(source: String) = ShaderSource(source)
    }
}

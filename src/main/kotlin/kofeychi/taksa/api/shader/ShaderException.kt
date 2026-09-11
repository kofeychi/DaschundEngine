package kofeychi.taksa.api.shader

class ShaderException(
    message: String,
    val stage: String? = null,
    val numberedSource: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    companion object {
        fun compiler(
            stage: String,
            source: String,
            log: String,
        ): ShaderException =
            ShaderException(
                message = buildString {
                    append("Failed to compile $stage shader.")
                    if (log.isNotBlank()) append("\nGL log:\n").append(log)
                    append("\nSource:\n").append(ShaderSource.numbered(source))
                },
                stage = stage,
                numberedSource = ShaderSource.numbered(source),
            )

        fun linker(log: String): ShaderException =
            ShaderException(
                if (log.isBlank()) "Failed to link shader program." else "Failed to link shader program.\nGL log:\n$log"
            )
    }
}

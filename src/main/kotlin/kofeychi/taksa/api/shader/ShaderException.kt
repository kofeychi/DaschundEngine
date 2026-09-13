package kofeychi.taksa.api.shader

/**
 * Thrown on shader compile / program link failure. [infoLog] is the raw driver
 * message; [annotatedSource], when available, is the offending GLSL source with
 * line numbers so the message you actually see points straight at the problem
 * instead of forcing you to count lines by hand.
 */
class ShaderException private constructor(
    message: String,
    val infoLog: String? = null,
    val annotatedSource: String? = null,
) : RuntimeException(message) {

    constructor(message: String) : this(message, null, null)

    companion object {
        private val ERROR_LINE_REGEX = Regex("""(?:ERROR|error)[^0-9]*(\d+)\s*:\s*(\d+)""")

        /**
         * Builds an exception whose message embeds the GLSL source with line numbers,
         * plus little `^^^ here` markers under every line the compiler flagged.
         */
        fun fromCompileLog(shaderType: String, source: String, infoLog: String): ShaderException {
            val flaggedLines = ERROR_LINE_REGEX.findAll(infoLog)
                .mapNotNull { it.groupValues.getOrNull(2)?.toIntOrNull() }
                .toSet()

            val numbered = source.lineSequence().mapIndexed { index, line ->
                val lineNo = index + 1
                val marker = if (lineNo in flaggedLines) ">>" else "  "
                "$marker %4d| %s".format(lineNo, line)
            }.joinToString("\n")

            val message = buildString {
                appendLine("Failed to compile $shaderType shader.")
                appendLine("Info Log:")
                appendLine(infoLog.trimEnd())
                appendLine()
                appendLine("Source:")
                append(numbered)
            }

            return ShaderException(message, infoLog, numbered)
        }

        fun fromLinkLog(infoLog: String): ShaderException =
            ShaderException("Failed to link shader program.\nInfo Log:\n$infoLog", infoLog, null)
    }
}

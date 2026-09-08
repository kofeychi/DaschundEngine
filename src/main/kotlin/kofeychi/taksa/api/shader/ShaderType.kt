package kofeychi.taksa.api.shader

import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL32
import org.lwjgl.opengl.GL43

enum class ShaderType(val glEnum: Int) {
    VERTEX(GL20.GL_VERTEX_SHADER),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER),
    GEOMETRY(GL32.GL_GEOMETRY_SHADER),
    TESS_CONTROL(GL43.GL_TESS_CONTROL_SHADER),
    TESS_EVALUATION(GL43.GL_TESS_EVALUATION_SHADER),
    COMPUTE(GL43.GL_COMPUTE_SHADER)
}
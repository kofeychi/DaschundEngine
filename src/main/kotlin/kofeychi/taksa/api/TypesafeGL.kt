package kofeychi.taksa.api

import org.lwjgl.opengl.GL20.glUseProgram

object TypesafeGL {
    @JvmInline value class ProgramId(val id: Int)

    fun useProgram(programId: ProgramId) {
        glUseProgram(programId.id)
    }
}
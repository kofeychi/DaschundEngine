package kofeychi.taksa.api.layer

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14

enum class BlendMode(
    val srcRgb: Int,
    val dstRgb: Int,
    val srcAlpha: Int,
    val dstAlpha: Int,
) {
    OPAQUE(GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_ZERO),
    ALPHA(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA),
    PREMULTIPLIED(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA),
    ADDITIVE(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE),
    MULTIPLY(GL11.GL_DST_COLOR, GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

    fun apply() {
        if (this == OPAQUE) org.lwjgl.opengl.GL11.glDisable(GL11.GL_BLEND)
        else {
            org.lwjgl.opengl.GL11.glEnable(GL11.GL_BLEND)
            GL14.glBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha)
        }
    }
}

package kofeychi.taksa.api.texture

object TextureExamples {
    fun rgba8(width: Int, height: Int): Texture =
        Texture.rgba(width, height) {
            minFilter = kofeychi.taksa.api.TextureFilter.LINEAR
            magFilter = kofeychi.taksa.api.TextureFilter.LINEAR
        }
}

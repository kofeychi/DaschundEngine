package kofeychi.taksa.api.texture

/**
 * Example:
 *
 * val texture = Texture.rgba(256, 256) {
 *     minFilter = TextureFilter.LINEAR_MIPMAP_LINEAR
 *     magFilter = TextureFilter.LINEAR
 *     wrapS = TextureWrap.REPEAT
 *     wrapT = TextureWrap.REPEAT
 *     generateMipmaps = true
 * }
 *
 * texture.edit {
 *     fill()
 *     set(10, 10, 255, 0, 0, 255)
 *     set(11, 10, 255, 255, 0, 255)
 * }.upload()
 *
 * // Or replace the entire image without replacing the Texture:
 * texture.upload(otherContents)
 */
object TextureExamples

package kofeychi.taksa.api.layer

import kofeychi.taksa.api.*
import kofeychi.taksa.api.shader.Program
import kofeychi.taksa.api.vertex.Format
import kofeychi.taksa.api.vertex.buffer.Mesh
import kofeychi.taksa.api.vertex.builder.*
import java.io.Closeable

class Layer(
    val program: Program,
    val drawMode: DrawMode,
    val format: Format,
    val bufferUsage: BufferUsage = BufferUsage.STREAM_DRAW,
    val builderFactory: () -> Builder = {
        DirectBuilder(4096,format)
    },
) : Closeable{
    private var mesh: Mesh? = null
    private var builder: Builder? = null

    fun builder(): Builder {
        if(builder == null) builder = builderFactory()
        return builder!!
    }

    fun mesh(): Mesh {
        if(mesh == null) mesh = Mesh(format,drawMode,bufferUsage)
        return mesh!!
    }


    internal fun flush() {
        val current = builder ?: return

        try {
            current.view().use { slice ->
                mesh().upload(slice)
            }
        } catch (e: IllegalStateException) {
            if (!e.message.orEmpty().contains("no vertices", ignoreCase = true)) throw e
            builder = null
            return
        } finally {
            builder?.clear()
            builder = null
        }
        program.bind()
        mesh().bind()
        mesh().draw()
        mesh().unbind()
        program.unbind()
    }

    override fun close() {
        builder?.close()
        mesh?.close()
    }
}
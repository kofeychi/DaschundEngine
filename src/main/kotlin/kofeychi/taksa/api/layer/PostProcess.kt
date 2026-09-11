package kofeychi.taksa.api.layer

fun interface PostProcess : AutoCloseable {
    fun apply(ctx: DrawCtx, input: Framebuffer, output: RenderTarget)
    override fun close() = Unit
}

class PostProcessPipeline : CompositePass {
    private val passes = ArrayList<PostProcess>()

    fun add(pass: PostProcess): PostProcessPipeline {
        passes += pass
        return this
    }

    override fun composite(ctx: DrawCtx, source: Framebuffer, destination: RenderTarget?) {
        val output = destination ?: throw IllegalStateException(
            "A post-process pipeline needs a destination RenderTarget; supply a window target/compositor at the root."
        )
        var current = source
        for (pass in passes) {
            pass.apply(ctx, current, output)
            if (output is Framebuffer) current = output
        }
    }

    override fun close() {
        passes.asReversed().forEach(PostProcess::close)
        passes.clear()
    }
}

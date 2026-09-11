package kofeychi.taksa.api.vertex.builder

import kofeychi.taksa.api.vertex.Format

class DirectBuilder(
    initialCapacity: Int = 4096,
    format: Format,
) : AbstractVertexBuilder(initialCapacity, format)

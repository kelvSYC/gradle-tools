package com.kelvsyc.gradle.ant

import java.io.Serializable

/**
 * A resolved Ant element: strings only, safe to pass to a work action.
 */
class AntNode(
    /** The element name. */
    val name: String,
    /** The element namespace URI, or `null` for the default namespace. */
    val namespace: String?,
    /** The attributes, in insertion order. */
    val attributes: Map<String, String>,
    /** The element text, or `null` if none. */
    val text: String?,
    /** The child elements, in order. */
    val children: List<AntNode>
) : Serializable {
    /**
     * Converts this node to JDK-only maps and lists, the form the Ant shim reads.
     */
    fun toShimMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "namespace" to namespace,
        "attributes" to attributes,
        "text" to text,
        "children" to children.map { it.toShimMap() }
    )

    companion object {
        private const val serialVersionUID = 1L
    }
}

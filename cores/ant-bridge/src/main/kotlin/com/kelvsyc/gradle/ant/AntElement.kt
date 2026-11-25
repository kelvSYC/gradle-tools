package com.kelvsyc.gradle.ant

import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import java.io.File
import java.nio.file.Path

/**
 * Thrown when an attribute or the text of an [AntElement] has no value at resolution time.
 */
class AntAttributeException(message: String) : GradleException(message)

/**
 * A configuration-side Ant element. Values may be strings, numbers, booleans, files, paths, file system locations,
 * file collections or providers of these, and stay unresolved until [resolve]. Deliberately not
 * [java.io.Serializable], so that the configuration cache uses its bean codec and keeps providers lazy.
 */
class AntElement(
    /** The element name. */
    val name: String,
    /** The element namespace URI, or `null` for the default namespace. */
    val namespace: String? = null
) {
    /** The attributes, in insertion order. */
    val attributes: MutableMap<String, Any?> = LinkedHashMap()

    /** The element text, or `null` if none. */
    var text: Any? = null

    /** The child elements, in order. */
    val children: MutableList<AntElement> = ArrayList()

    /**
     * Resolves every value to a string and returns the serializable tree.
     *
     * @throws AntAttributeException if an attribute or the text has no value
     */
    fun resolve(): AntNode {
        val resolved = LinkedHashMap<String, String>()
        attributes.forEach { (key, value) -> resolved[key] = resolveValue(value, "$name@$key") }
        val resolvedText = text?.let { resolveValue(it, "$name text") }
        return AntNode(name, namespace, resolved, resolvedText, children.map { it.resolve() })
    }

    private fun resolveValue(value: Any?, description: String): String = when (value) {
        null -> throw noValue(description)
        is Provider<*> -> resolveValue(value.orNull, description)
        is File -> value.absolutePath
        is Path -> value.toAbsolutePath().toString()
        is FileSystemLocation -> value.asFile.absolutePath
        is FileCollection -> value.asPath
        else -> value.toString()
    }

    private fun noValue(description: String) = AntAttributeException("$description has no value")
}

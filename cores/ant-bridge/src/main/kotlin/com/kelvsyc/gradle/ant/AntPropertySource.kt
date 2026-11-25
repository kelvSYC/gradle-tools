package com.kelvsyc.gradle.ant

/**
 * One source of [AntProperties], loaded as Ant's `property` task would load it.
 */
sealed class AntPropertySource : java.io.Serializable {
    /**
     * Converts this source to JDK-only maps and lists, the form the Ant shim reads.
     */
    abstract fun toShimMap(): Map<String, Any?>

    /**
     * A properties file (ISO-8859-1, as Ant reads it). A missing file contributes nothing.
     *
     * @property file the file
     * @property prefix prepended, with a `.`, to every key (Ant's `prefix`)
     * @property prefixValues whether references in values resolve against prefixed keys (Ant's `prefixValues`)
     */
    data class File(val file: java.io.File, val prefix: String?, val prefixValues: Boolean) : AntPropertySource() {
        override fun toShimMap(): Map<String, Any?> = mapOf(
            "kind" to "file",
            "file" to file.absolutePath,
            "prefix" to prefix,
            "prefixValues" to prefixValues
        )
    }

    /**
     * A properties resource looked up on [classpath].
     *
     * @property name the resource name
     * @property classpath where the resource is looked up
     * @property prefix prepended, with a `.`, to every key (Ant's `prefix`)
     * @property prefixValues whether references in values resolve against prefixed keys (Ant's `prefixValues`)
     */
    data class Resource(
        val name: String,
        val classpath: List<java.io.File>,
        val prefix: String?,
        val prefixValues: Boolean
    ) : AntPropertySource() {
        override fun toShimMap(): Map<String, Any?> = mapOf(
            "kind" to "resource",
            "name" to name,
            "classpath" to classpath.map { it.absolutePath },
            "prefix" to prefix,
            "prefixValues" to prefixValues
        )
    }

    /**
     * A properties document read from a URL.
     *
     * @property url the URL
     * @property prefix prepended, with a `.`, to every key (Ant's `prefix`)
     * @property prefixValues whether references in values resolve against prefixed keys (Ant's `prefixValues`)
     */
    data class Url(val url: String, val prefix: String?, val prefixValues: Boolean) : AntPropertySource() {
        override fun toShimMap(): Map<String, Any?> = mapOf(
            "kind" to "url",
            "url" to url,
            "prefix" to prefix,
            "prefixValues" to prefixValues
        )
    }

    /**
     * The environment variables, each as `<prefix>.<NAME>` (Ant's `environment`).
     *
     * @property prefix the prefix
     */
    data class Environment(val prefix: String) : AntPropertySource() {
        override fun toShimMap(): Map<String, Any?> = mapOf("kind" to "environment", "prefix" to prefix)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}

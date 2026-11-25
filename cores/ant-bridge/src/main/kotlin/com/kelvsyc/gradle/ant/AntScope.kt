package com.kelvsyc.gradle.ant

import org.gradle.api.Action

/**
 * The top-level scope of the Kotlin DSL: each call appends one Ant task to the tree being built.
 */
class AntScope internal constructor(private val sink: MutableList<AntElement>) {
    /**
     * Appends an Ant task.
     *
     * @param name the task name
     * @param attributes the task attributes, in order
     * @param namespace the task namespace URI, or `null` for the default namespace
     * @param body configures nested elements
     */
    fun task(
        name: String,
        vararg attributes: Pair<String, Any?>,
        namespace: String? = null,
        body: Action<in AntElementScope> = Action {}
    ) {
        val element = AntElement(name, namespace)
        attributes.forEach { (key, value) -> element.attributes[key] = value }
        sink.add(element)
        body.execute(AntElementScope(element))
    }
}

/**
 * The scope of one Ant element: adds nested elements, text and typed helpers for common Ant types.
 */
class AntElementScope internal constructor(
    /** The element being configured. */
    val element: AntElement
) {
    /**
     * Appends a nested element.
     *
     * @param name the element name
     * @param attributes the element attributes, in order
     * @param namespace the element namespace URI, or `null` for the default namespace
     * @param body configures nested elements
     */
    fun element(
        name: String,
        vararg attributes: Pair<String, Any?>,
        namespace: String? = null,
        body: Action<in AntElementScope> = Action {}
    ) {
        val child = AntElement(name, namespace)
        attributes.forEach { (key, value) -> child.attributes[key] = value }
        element.children.add(child)
        body.execute(AntElementScope(child))
    }

    /**
     * Sets the text of the element.
     *
     * @param value the text, resolved at execution
     */
    fun text(value: Any) {
        element.text = value
    }

    /**
     * Adds a `fileset`: include children first, then exclude children, then the children of [body].
     *
     * @param dir the base directory
     * @param includes include patterns
     * @param excludes exclude patterns
     * @param body configures further nested elements
     */
    fun fileset(
        dir: Any,
        includes: List<String> = emptyList(),
        excludes: List<String> = emptyList(),
        body: Action<in AntElementScope> = Action {}
    ) {
        element("fileset", "dir" to dir) {
            includes.forEach { include(it) }
            excludes.forEach { exclude(it) }
            body.execute(this)
        }
    }

    /**
     * Adds an `include` element.
     *
     * @param name the pattern
     */
    fun include(name: String) {
        element("include", "name" to name)
    }

    /**
     * Adds an `exclude` element.
     *
     * @param name the pattern
     */
    fun exclude(name: String) {
        element("exclude", "name" to name)
    }

    /**
     * Adds a `path` element; the value stays lazy until resolution.
     *
     * @param files the path value, such as a `FileCollection`
     */
    fun path(files: Any) {
        element("path", "path" to files)
    }

    /**
     * Adds a `mapper` element, omitting absent attributes.
     *
     * @param type the mapper type
     * @param from the `from` pattern, if any
     * @param to the `to` pattern, if any
     */
    fun mapper(type: String, from: String? = null, to: String? = null) {
        element("mapper", "type" to type) {
            from?.let { element.attributes["from"] = it }
            to?.let { element.attributes["to"] = it }
        }
    }

    /**
     * Adds a `filterset` with one `filter` child per token, in map order.
     *
     * @param beginToken the begin token
     * @param endToken the end token
     * @param tokens the token values by name
     */
    fun filterset(beginToken: String = "@", endToken: String = "@", tokens: Map<String, Any>) {
        element("filterset", "begintoken" to beginToken, "endtoken" to endToken) {
            tokens.forEach { (token, value) -> element("filter", "token" to token, "value" to value) }
        }
    }

    /**
     * Adds a `filterchain` element.
     *
     * @param body configures the filters
     */
    fun filterchain(body: Action<in AntElementScope>) {
        element("filterchain", body = body)
    }
}

/**
 * Runs [body] against a fresh [AntScope] and returns the elements it appended.
 */
internal fun buildTree(body: Action<in AntScope>): List<AntElement> {
    val sink = ArrayList<AntElement>()
    body.execute(AntScope(sink))
    return sink
}


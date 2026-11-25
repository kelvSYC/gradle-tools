package com.kelvsyc.gradle.ant

import org.gradle.api.Action

/**
 * The top-level scope of the Kotlin DSL: each call appends one Ant task to the tree being built.
 *
 * In attributes, a `null` value leaves the attribute unset (convenient for optional values such as
 * `encoding.orNull`); a `Provider` stays lazy, and an absent one fails when the tree is resolved.
 *
 * To reuse an Ant task across builds, define a task type by extending
 * [com.kelvsyc.gradle.ant.tasks.AbstractAntTask] rather than an extension function on this scope: see "Reusing Ant
 * code" in the README.
 */
class AntScope internal constructor(private val sink: MutableList<AntElement>) {
    /**
     * Appends an Ant task.
     *
     * @param name the task name
     * @param attributes the task attributes, in order; `null` values are left out
     * @param namespace the task namespace URI, or `null` for the default namespace
     * @param body configures nested elements
     */
    fun task(
        name: String,
        vararg attributes: Pair<String, Any?>,
        namespace: String? = null,
        body: Action<in AntElementScope> = Action {}
    ) {
        sink.add(newElement(name, namespace, attributes, body))
    }
}

private fun newElement(
    name: String,
    namespace: String?,
    attributes: Array<out Pair<String, Any?>>,
    body: Action<in AntElementScope>
): AntElement = AntElement(name, namespace).also { element ->
    attributes.forEach { (key, value) -> if (value != null) element.attributes[key] = value }
    body.execute(AntElementScope(element))
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
     * @param attributes the element attributes, in order; `null` values are left out
     * @param namespace the element namespace URI, or `null` for the default namespace
     * @param body configures nested elements
     */
    fun element(
        name: String,
        vararg attributes: Pair<String, Any?>,
        namespace: String? = null,
        body: Action<in AntElementScope> = Action {}
    ) {
        element.children.add(newElement(name, namespace, attributes, body))
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
        element("mapper", "type" to type, "from" to from, "to" to to)
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


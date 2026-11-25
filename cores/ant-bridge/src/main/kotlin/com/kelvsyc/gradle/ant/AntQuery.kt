package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.internal.AntRuntimeClassLoader
import com.kelvsyc.gradle.ant.internal.AntShim
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import javax.inject.Inject

/**
 * Runs an Ant tree in a fresh Ant project, in an isolated class loader, and returns the [requested][Parameters.results]
 * properties the tree set. Names the tree did not set are absent from the map.
 *
 * The configuration cache obtains the value again on every build to validate its entry, so the tree re-runs on every
 * build, even when the cache is reused. An Ant failure fails the build with [AntExecutionException].
 */
abstract class AntQuery : ValueSource<Map<String, String>, AntQuery.Parameters> {
    /**
     * Parameters of [AntQuery].
     */
    interface Parameters : ValueSourceParameters {
        /** The resolved Ant tree, run in order. */
        val nodes: ListProperty<AntNode>

        /** The names of the properties to return. */
        val results: SetProperty<String>

        /** The Ant classpath: Ant and anything the tree's Ant tasks need. */
        val antClasspath: ConfigurableFileCollection

        /** The Ant project's base directory (`basedir`); `antBridge.query` sets it to the project directory. */
        val baseDirectory: DirectoryProperty
    }

    override fun obtain(): Map<String, String> =
        AntRuntimeClassLoader(parameters.antClasspath.files.toList()).use { loader ->
            AntShim(loader).execute(
                parameters.nodes.get(),
                parameters.baseDirectory.get().asFile,
                Logging.getLogger(AntQuery::class.java),
                parameters.results.get()
            ).properties
        }
}

/** Read-only Ant tasks allowed at the top level of a query without [AntQuerySpec.allowSideEffects]. */
private val READ_ONLY_TASKS = setOf(
    "available", "condition", "loadfile", "loadproperties", "property", "basename", "dirname", "pathconvert", "length"
)

/** Allowed only with a `property` attribute; without it, `checksum` writes files. */
private const val CHECKSUM = "checksum"

/**
 * Declares the Ant tree and the requested properties of an [AntQuery]. Not designed to be extended.
 *
 * The query runs while the build is configured and the configuration cache re-runs it on every build, so only
 * read-only Ant tasks are allowed at the top level of the tree (`available`, `condition`, `loadfile`,
 * `loadproperties`, `property`, `basename`, `dirname`, `pathconvert`, `length`, and `checksum` with a `property`
 * attribute) unless [allowSideEffects] is called. Nested elements of an allowed task are not checked.
 */
abstract class AntQuerySpec @Inject constructor(private val layout: ProjectLayout) {
    private val elements = mutableListOf<AntElement>()
    private val results = linkedSetOf<String>()
    private var sideEffectsAllowed = false

    /**
     * Appends the Ant tasks of [body], written in the Kotlin DSL. The tree re-runs on every build.
     */
    fun ant(body: Action<in AntScope>) {
        elements += AntTrees.build(body)
    }

    /**
     * Appends the legacy `ant.xxx(...)` calls recorded by [body], such as a Kotlin `withGroovyBuilder { }` block. The
     * tree re-runs on every build.
     */
    fun record(body: Action<in AntRecorder>) {
        elements += AntTrees.record(body)
    }

    /**
     * Appends the legacy `ant.xxx(...)` calls recorded by the Groovy closure [body], which runs with the recorder as
     * its delegate, so its body is unchanged from a legacy `ant { }` block. The tree re-runs on every build.
     */
    fun record(body: Closure<*>) {
        elements += AntTrees.record(body)
    }

    /**
     * Requests the properties [names]; the query's map holds those the tree set, with their values.
     */
    fun result(vararg names: String) {
        results += names
    }

    /**
     * Allows any Ant task in the tree. The tree re-runs on every build, including those that reuse the configuration
     * cache, so its side effects repeat on every build; prefer `AntWork.exportProperties` for work that belongs in a
     * task.
     */
    fun allowSideEffects() {
        sideEffectsAllowed = true
    }

    internal fun applyTo(parameters: AntQuery.Parameters) {
        if (!sideEffectsAllowed) {
            elements.forEach(::checkReadOnly)
        }
        parameters.nodes.set(elements.map { it.resolve() })
        parameters.results.set(results)
        parameters.baseDirectory.set(layout.projectDirectory)
    }

    private fun checkReadOnly(element: AntElement) {
        val allowed = element.namespace == null && (
            element.name in READ_ONLY_TASKS || element.name == CHECKSUM && "property" in element.attributes
        )
        if (!allowed) {
            val task = when {
                element.namespace != null -> "'${element.name}' (namespace ${element.namespace})"
                element.name == CHECKSUM -> "'$CHECKSUM' without a 'property' attribute"
                else -> "'${element.name}'"
            }
            throw InvalidUserDataException(
                "Ant task $task is not allowed in antBridge.query: the configuration cache re-runs the query on " +
                    "every build, so its side effects would repeat. Call allowSideEffects() to run it anyway, or " +
                    "move the work into a task and export its properties with AntWork.exportProperties."
            )
        }
    }
}

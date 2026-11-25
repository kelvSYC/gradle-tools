package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.ant.AntExecutionException
import com.kelvsyc.gradle.ant.AntNode
import com.kelvsyc.gradle.ant.AntPropertySource
import org.gradle.api.logging.Logger
import java.io.File
import java.util.function.BiConsumer
import java.util.function.Function

/**
 * Invokes the Ant shim's entry points in [loader] by class name, exchanging JDK-typed maps only.
 *
 * The boundary contract (request and result keys, failures as values, the context class loader) is described in the
 * core's `INTERNALS.md`.
 */
class AntShim(
    /** The isolated loader holding Ant and the shim. */
    private val loader: AntRuntimeClassLoader
) {
    /**
     * Runs [nodes] in order in a fresh Ant project based at [baseDirectory], logging Ant's messages to [logger], and
     * returns the version Ant reports (`null` if it reports none) and the values of the [results] names the project
     * defines after the run.
     *
     * @throws AntExecutionException if Ant reports a failure
     */
    fun execute(
        nodes: List<AntNode>,
        baseDirectory: File,
        logger: Logger,
        results: Set<String> = emptySet()
    ): TreeResult {
        val request = mapOf(
            "nodes" to nodes.map { it.toShimMap() },
            "baseDirectory" to baseDirectory.absolutePath,
            "results" to results.toList()
        )
        val result = invoke(TREE_EXECUTOR, request, logger)
        @Suppress("UNCHECKED_CAST")
        val properties = (result["properties"] as Map<String, String>?).orEmpty()
        return TreeResult(result["antVersion"]?.toString(), LinkedHashMap(properties))
    }

    /**
     * Loads the properties of [request] in a fresh Ant project based at its base directory, logging Ant's messages to [logger], and returns the
     * seeds and every key a source defined, in that order, with Ant's effective value.
     *
     * @throws AntExecutionException if Ant reports a failure
     */
    fun loadProperties(request: PropertyRequest, logger: Logger): Map<String, String> {
        val shimRequest = mapOf(
            "seeds" to request.seeds,
            "sources" to request.sources.map { it.toShimMap() },
            "baseDirectory" to request.baseDirectory.absolutePath
        )
        val result = invoke(PROPERTY_LOADER, shimRequest, logger)
        @Suppress("UNCHECKED_CAST")
        return result["properties"] as Map<String, String>
    }

    /**
     * Parses [files] in order into one fresh Ant project, with [seeds] set first as user properties, logging Ant's
     * messages to [logger], and returns the targets, top-level properties and loaded files of the parse. No target
     * runs.
     *
     * Before each file, the project's base directory is the file's own `<project basedir>`, else
     * [BuildFile.baseDirectory], else the file's directory.
     *
     * @throws AntExecutionException if Ant reports a failure, or a target name is defined in two top-level files
     */
    fun readBuild(files: List<BuildFile>, seeds: Map<String, String>, logger: Logger): BuildModel {
        val request = mapOf("files" to files.map { it.toShimMap() }, "seeds" to seeds)
        val result = invoke(BUILD_READER, request, logger)
        @Suppress("UNCHECKED_CAST")
        val targets = (result["targets"] as List<Map<String, Any?>>).map { target ->
            @Suppress("UNCHECKED_CAST")
            BuildTarget(
                name = target["name"] as String,
                file = File(target["file"] as String),
                description = target["description"] as String?,
                depends = (target["depends"] as List<String>).toList(),
                upstream = (target["upstream"] as List<String>).toList()
            )
        }
        @Suppress("UNCHECKED_CAST")
        val properties = LinkedHashMap(result["properties"] as Map<String, String>)
        @Suppress("UNCHECKED_CAST")
        val loadedFiles = (result["loadedFiles"] as List<String>).map(::File)
        return BuildModel(targets, properties, loadedFiles)
    }

    /**
     * Runs only [target]'s own tasks, not its Ant dependencies, in a fresh Ant project and returns every property the
     * run defined. The project gets [seeds] as user properties, then each map of [upstreamExports] in order (the first
     * definition of a name wins), then parses [files] as [readBuild] does. The target's own `if` and `unless` apply.
     *
     * @throws AntExecutionException if Ant reports a failure, or the build has no such target
     */
    fun runTarget(
        files: List<BuildFile>,
        seeds: Map<String, String>,
        upstreamExports: List<Map<String, String>>,
        target: String,
        logger: Logger
    ): Map<String, String> {
        val request = mapOf(
            "files" to files.map { it.toShimMap() },
            "seeds" to seeds,
            "exports" to upstreamExports,
            "target" to target
        )
        val result = invoke(TARGET_RUNNER, request, logger)
        @Suppress("UNCHECKED_CAST")
        return LinkedHashMap(result["properties"] as Map<String, String>)
    }

    /**
     * Invokes the shim entry point [className] and returns its result.
     *
     * @throws AntExecutionException if the result reports a failure
     */
    private fun invoke(className: String, request: Map<String, Any?>, logger: Logger): Map<String, Any?> {
        val result = apply(className, request, logger)
        if (result.containsKey("failure")) {
            val message = result["failure"]?.toString() ?: "Ant failed"
            val location = result["location"]?.toString().orEmpty()
            result["trace"]?.let { logger.debug("Ant failure stack trace:\n$it") }
            throw AntExecutionException(if (location.isEmpty()) message else "$location$message")
        }
        return result
    }

    private fun apply(className: String, request: Map<String, Any?>, logger: Logger): Map<String, Any?> {
        val thread = Thread.currentThread()
        val previous = thread.contextClassLoader
        thread.contextClassLoader = loader
        try {
            @Suppress("UNCHECKED_CAST")
            val entryPoint = loader.loadClass(className).getDeclaredConstructor().newInstance()
                as Function<Map<String, Any?>, Map<String, Any?>>
            return entryPoint.apply(request + ("logger" to LoggerBridge(logger)))
        } finally {
            thread.contextClassLoader = previous
        }
    }

    /**
     * The outcome of [execute].
     */
    data class TreeResult(
        /** The version Ant reports, or `null` if it reports none. */
        val antVersion: String?,
        /** The requested names the project defines, with their values, in request order. */
        val properties: Map<String, String>
    )

    /**
     * A property-loading request: [seeds] are set first, so they override the [sources], which load in order.
     */
    data class PropertyRequest(
        /** The sources, in order. */
        val sources: List<AntPropertySource>,
        /** Properties set before any source. */
        val seeds: Map<String, String>,
        /** The Ant project's base directory (`basedir`). */
        val baseDirectory: File
    )

    /**
     * A top-level build file to parse.
     */
    data class BuildFile(
        /** The build file. */
        val file: File,
        /** The base directory, or `null` when the file's own `<project basedir>` or its directory applies. */
        val baseDirectory: File?
    ) {
        internal fun toShimMap(): Map<String, String?> =
            mapOf("file" to file.absolutePath, "baseDirectory" to baseDirectory?.absolutePath)
    }

    /**
     * The outcome of [readBuild].
     */
    data class BuildModel(
        /** Every target name Ant registered, prefixed `<import>` aliases included, in name order. */
        val targets: List<BuildTarget>,
        /**
         * The seeds and the properties the parse defined, with Ant's effective value. Ant built-ins and system
         * properties the build did not name are excluded.
         */
        val topLevelProperties: Map<String, String>,
        /** The top-level files and every file they imported or included. */
        val loadedFiles: List<File>
    )

    /**
     * A target of a [BuildModel].
     */
    data class BuildTarget(
        /** The name Ant registered the target under. */
        val name: String,
        /** The file defining the target. */
        val file: File,
        /** The target's description, or `null` if it has none. */
        val description: String?,
        /** The direct dependencies as Ant resolved them, extension-point wiring included. */
        val depends: List<String>,
        /** Every transitive dependency that is a target, in Ant's execution order, without the target itself. */
        val upstream: List<String>
    )

    private class LoggerBridge(private val logger: Logger) : BiConsumer<Int, String> {
        override fun accept(priority: Int, message: String) {
            when (priority) {
                MSG_ERR -> logger.error(message)
                MSG_WARN -> logger.warn(message)
                MSG_INFO -> logger.info(message)
                else -> logger.debug(message)
            }
        }
    }

    private companion object {
        const val TREE_EXECUTOR = "com.kelvsyc.gradle.ant.shim.TreeExecutor"
        const val PROPERTY_LOADER = "com.kelvsyc.gradle.ant.shim.PropertyLoader"
        const val BUILD_READER = "com.kelvsyc.gradle.ant.shim.BuildReader"
        const val TARGET_RUNNER = "com.kelvsyc.gradle.ant.shim.TargetRunner"

        /** Ant's `Project.MSG_ERR`. */
        const val MSG_ERR = 0

        /** Ant's `Project.MSG_WARN`. */
        const val MSG_WARN = 1

        /** Ant's `Project.MSG_INFO`. */
        const val MSG_INFO = 2
    }
}

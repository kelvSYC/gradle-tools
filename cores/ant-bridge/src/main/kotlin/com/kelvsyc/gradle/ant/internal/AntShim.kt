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
 */
class AntShim(
    /** The isolated loader holding Ant and the shim. */
    private val loader: AntRuntimeClassLoader
) {
    /**
     * Runs [nodes] in order in a fresh Ant project based at [baseDirectory], logging Ant's messages to [logger], and
     * returns the version Ant reports, or `null` if it reports none.
     *
     * @throws AntExecutionException if Ant reports a failure
     */
    fun execute(nodes: List<AntNode>, baseDirectory: File, logger: Logger): String? {
        val request = mapOf(
            "nodes" to nodes.map { it.toShimMap() },
            "baseDirectory" to baseDirectory.absolutePath
        )
        return invoke(TREE_EXECUTOR, request, logger)["antVersion"]?.toString()
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

        /** Ant's `Project.MSG_ERR`. */
        const val MSG_ERR = 0

        /** Ant's `Project.MSG_WARN`. */
        const val MSG_WARN = 1

        /** Ant's `Project.MSG_INFO`. */
        const val MSG_INFO = 2
    }
}

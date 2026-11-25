package com.kelvsyc.gradle.ant.internal

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Caches one isolated Ant class loader per Ant classpath for the duration of a build, so that repeated
 * [com.kelvsyc.gradle.ant.AntProperties] obtains reuse it. The loaders are closed when the build finishes.
 */
abstract class AntRuntimeService : BuildService<BuildServiceParameters.None>, AutoCloseable {
    private val loaders = ConcurrentHashMap<List<File>, AntRuntimeClassLoader>()

    /**
     * Returns a shim over the loader for [classpath], creating the loader on first use.
     */
    fun shim(classpath: List<File>): AntShim = AntShim(loaders.computeIfAbsent(classpath, ::AntRuntimeClassLoader))

    override fun close() {
        loaders.values.forEach { it.close() }
        loaders.clear()
    }
}

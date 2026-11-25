package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.internal.AntRuntimeClassLoader
import com.kelvsyc.gradle.ant.internal.AntShim
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import javax.inject.Inject

/**
 * Loads properties as Ant's `property` task does, in a fresh Ant project run in an isolated class loader.
 *
 * The [seeds][Parameters.seeds] are set first, then each [source][Parameters.sources] is loaded in order. As in Ant,
 * the first value set for a key wins, and values may reference properties set earlier or in the same source. The
 * result holds only the keys the seeds and sources name, each with Ant's effective value, which for a key that was
 * already set (by a system property, a seed or an earlier source) is the earlier value. A missing file contributes
 * nothing and is logged at info.
 *
 * Files, URLs and the environment are read only when the value is obtained, so the configuration cache is
 * invalidated when they change.
 */
abstract class AntProperties : ValueSource<Map<String, String>, AntProperties.Parameters> {
    /**
     * Parameters of [AntProperties].
     */
    interface Parameters : ValueSourceParameters {
        /** The sources, loaded in order. */
        val sources: ListProperty<AntPropertySource>

        /** Properties set before any source, so they override the sources' values. */
        val seeds: MapProperty<String, String>

        /** The Ant project's base directory (`basedir`); `antBridge.properties` sets it to the project directory. */
        val baseDirectory: DirectoryProperty

        /** The Ant classpath: Ant and anything the sources' Ant tasks need. */
        val antClasspath: ConfigurableFileCollection
    }

    override fun obtain(): Map<String, String> =
        AntRuntimeClassLoader(parameters.antClasspath.files.toList()).use { loader ->
            val request = AntShim.PropertyRequest(
                parameters.sources.get(),
                parameters.seeds.getOrElse(emptyMap()),
                parameters.baseDirectory.get().asFile
            )
            AntShim(loader).loadProperties(request, Logging.getLogger(AntProperties::class.java))
        }
}

/**
 * Per-source options of [AntPropertiesSpec.file], [AntPropertiesSpec.resource] and [AntPropertiesSpec.url], mirroring Ant's `property` task.
 */
abstract class AntPropertySourceOptions {
    /** Prepended, with a `.`, to every key (Ant's `prefix`). */
    abstract val prefix: Property<String>

    /** Whether references in values resolve against prefixed keys (Ant's `prefixValues`); `false` by default. */
    abstract val prefixValues: Property<Boolean>

    init {
        prefixValues.convention(false)
    }
}

/**
 * Declares the sources and seeds of [AntProperties]. Sources load in the order they are declared.
 */
abstract class AntPropertiesSpec @Inject constructor(private val layout: ProjectLayout) {
    /** Creates the per-source options. */
    @get:Inject
    protected abstract val objects: ObjectFactory

    private val sources = mutableListOf<AntPropertySource>()
    private val seeds = linkedMapOf<String, String>()

    /**
     * Adds a properties file, resolved against the project directory.
     */
    fun file(path: Any) = file(path) { }

    /**
     * Adds a properties file, resolved against the project directory, with [options].
     */
    fun file(path: Any, options: Action<in AntPropertySourceOptions>) {
        val configured = options(options)
        val file = layout.projectDirectory.files(path).singleFile
        sources += AntPropertySource.File(file, configured.prefix.orNull, configured.prefixValues.get())
    }

    /**
     * Adds a properties resource looked up on [classpath]. The classpath is resolved when this function is called,
     * at configuration time.
     */
    fun resource(name: String, classpath: FileCollection) = resource(name, classpath) { }

    /**
     * Adds a properties resource looked up on [classpath], with [options]. The classpath is resolved when this
     * function is called, at configuration time.
     */
    fun resource(name: String, classpath: FileCollection, options: Action<in AntPropertySourceOptions>) {
        val configured = options(options)
        sources += AntPropertySource.Resource(
            name,
            classpath.files.toList(),
            configured.prefix.orNull,
            configured.prefixValues.get()
        )
    }

    /**
     * Adds a properties document read from [url].
     */
    fun url(url: String) = url(url) { }

    /**
     * Adds a properties document read from [url], with [options].
     */
    fun url(url: String, options: Action<in AntPropertySourceOptions>) {
        val configured = options(options)
        sources += AntPropertySource.Url(url, configured.prefix.orNull, configured.prefixValues.get())
    }

    /**
     * Adds the environment variables, each as `<prefix>.<NAME>`.
     */
    fun environment(prefix: String) {
        sources += AntPropertySource.Environment(prefix)
    }

    /**
     * Sets [key] to [value] before any source is loaded, overriding the sources' values for it.
     */
    fun seed(key: String, value: String) {
        seeds[key] = value
    }

    private fun options(action: Action<in AntPropertySourceOptions>): AntPropertySourceOptions =
        objects.newInstance(AntPropertySourceOptions::class.java).also { action.execute(it) }

    internal fun applyTo(parameters: AntProperties.Parameters) {
        parameters.baseDirectory.set(layout.projectDirectory)
        parameters.sources.set(sources)
        parameters.seeds.set(seeds)
    }
}

package com.kelvsyc.gradle.ant

import com.kelvsyc.gradle.ant.internal.AntImportRegistry
import org.gradle.api.Action
import org.gradle.api.Transformer
import org.gradle.api.file.FileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import javax.inject.Inject

/**
 * The handle `antBridge.importBuild` returns for an imported Ant build.
 */
class AntImport internal constructor(private val registry: AntImportRegistry) {
    /**
     * Returns the properties [target] would have seen when it ran: the seeds, the top-level properties of the
     * project's Ant build, then the exports of its upstream targets in Ant's order (imported targets, and excluded
     * targets whose replacement supplies properties), the first definition of a name winning. It carries the
     * dependencies on those upstream tasks. [target] may be any target of the project's Ant build, imported or
     * excluded; an unknown target fails when the provider is queried.
     */
    fun propertiesFor(target: String): Provider<Map<String, String>> = registry.propertiesFor(target)
}

/**
 * Configures one `antBridge.importBuild` call. Not designed to be extended.
 */
abstract class AntImportSpec @Inject constructor(private val objects: ObjectFactory) {
    internal var taskNameTransformer: Transformer<String, String> = Transformer { it }
        private set

    internal val exclusions: MutableMap<String, AntExclusion?> = LinkedHashMap()

    internal var isolation: AntIsolation = AntIsolation.CLASSLOADER
        private set

    internal var antClasspath: FileCollection? = null
        private set

    private val seedSources = mutableListOf<() -> Map<String, String>>()

    /**
     * Names each target's task with [transformer], given the target name (the legacy renaming closure). It applies
     * to this import's targets, excluded ones included: an imported target depending on an excluded target depends
     * on the task of the transformed name, which the replacement must have.
     */
    fun taskName(transformer: Transformer<String, String>) {
        taskNameTransformer = transformer
    }

    /**
     * Registers no task for [targets]; the build defines their replacements, which imported targets find by (task)
     * name. Each must be a target this import adds.
     */
    fun exclude(vararg targets: String) {
        targets.forEach { exclusions.putIfAbsent(it, null) }
    }

    /**
     * Registers no task for [target], and configures with [configure] what its replacement supplies to the targets
     * downstream of it.
     */
    fun exclude(target: String, configure: Action<in AntExclusion>) {
        val exclusion = exclusions[target] ?: objects.newInstance(AntExclusion::class.java)
        configure.execute(exclusion)
        exclusions[target] = exclusion
    }

    /**
     * Sets [seeds] as Ant properties before the build files load, as legacy `ant.properties` writes before the
     * import did. Queried when the build is read, during configuration. The first definition of a name wins, also
     * across calls and imports.
     */
    fun properties(seeds: Provider<Map<String, String>>) {
        seedSources += { seeds.get() }
    }

    /**
     * Sets [seeds] as Ant properties before the build files load; see the `Provider` overload.
     */
    fun properties(seeds: Map<String, String>) {
        val copy = LinkedHashMap(seeds)
        seedSources += { copy }
    }

    /**
     * Runs this import's tasks isolated as [isolation] says. Defaults to [AntIsolation.CLASSLOADER].
     */
    fun isolation(isolation: AntIsolation) {
        this.isolation = isolation
    }

    /**
     * Reads the build and runs this import's tasks with [classpath]: Ant, its antlibs and anything they need.
     * Defaults to the `antBridge` classpath.
     */
    fun antClasspath(classpath: FileCollection) {
        antClasspath = classpath
    }

    /** Resolves the seeds, the first definition of a name winning. */
    internal fun resolveSeeds(): Map<String, String> {
        val result = LinkedHashMap<String, String>()
        seedSources.forEach { source -> source().forEach { (key, value) -> result.putIfAbsent(key, value) } }
        return result
    }
}

/**
 * Configures an excluded target of an `antBridge.importBuild` call. Not designed to be extended.
 */
abstract class AntExclusion {
    internal var supplied: Provider<Map<String, String>>? = null
        private set

    /**
     * Supplies [values] in place of the properties the excluded target used to define: the targets downstream of it
     * load them, as a declared input carrying [values]' task dependencies (typically on the replacement). Replaces
     * values supplied before.
     */
    fun properties(values: Provider<Map<String, String>>) {
        supplied = values
    }
}

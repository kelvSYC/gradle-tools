package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.ant.AntImport
import com.kelvsyc.gradle.ant.AntImportSpec
import com.kelvsyc.gradle.ant.AntImportedTarget
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * The Ant build of one Gradle project: every `antBridge.importBuild` call of the project adds a top-level file to it.
 * Configuration-only: it holds what the calls declared and what reading the build reported, and wires the imported
 * tasks lazily from the final state. Its properties are finalized when read, so nothing changes after configuration;
 * no task reads the registry at execution.
 */
internal class AntImportRegistry(
    private val tasks: TaskContainer,
    private val layout: ProjectLayout,
    private val providers: ProviderFactory,
    private val objects: ObjectFactory,
    private val defaultClasspath: FileCollection
) {
    /** Receives Ant's messages while reading the build, and the exclusion warnings. */
    var logger: Logger = Logging.getLogger(AntImportRegistry::class.java)

    private val files = mutableListOf<AntShim.BuildFile>()
    private val seedValues = LinkedHashMap<String, String>()
    private var targets: Map<String, AntShim.BuildTarget> = emptyMap()

    /** Target name to task name, excluded targets included. */
    private val taskNames = LinkedHashMap<String, String>()

    /** Excluded target to the task materialising the properties its replacement supplies, if any. */
    private val exclusions = LinkedHashMap<String, TaskProvider<AntExclusionExport>?>()

    private var topLevelPropertyValues: Map<String, String> = emptyMap()
    private var buildFileValues: List<File> = emptyList()

    /** Set once any of the properties below is read: the imported build can no longer change. */
    private var frozen = false

    // Each is finalized when first read, from the state above at that time.
    private val topLevelFiles = objects.listProperty(File::class.java)
        .value(frozenProvider { files.map { it.file } }).apply { finalizeValueOnRead() }
    private val baseDirectories = objects.listProperty(String::class.java)
        .value(frozenProvider { files.map { it.baseDirectory?.absolutePath.orEmpty() } })
        .apply { finalizeValueOnRead() }
    private val seeds = objects.mapProperty(String::class.java, String::class.java)
        .value(frozenProvider { seedValues.toMap() }).apply { finalizeValueOnRead() }
    private val topLevelProperties = objects.mapProperty(String::class.java, String::class.java)
        .value(frozenProvider { topLevelPropertyValues }).apply { finalizeValueOnRead() }
    // Not frozenProvider: resolving task dependencies queries it without finalizing it.
    private val buildFiles = objects.fileCollection()
        .from(providers.provider { buildFileValues }).apply { finalizeValueOnRead() }

    /**
     * Adds [file] (based at [baseDirectory], if given) to the build, re-reads the whole build, and registers a task
     * for every target the file adds, except the targets [spec] excludes.
     *
     * Every file the build loaded, and every existing file its `property file=` and `loadproperties` tasks read
     * during the parse, is a configuration cache input. Other inputs of the parse (environment, URLs, resources,
     * commands) are not tracked. Nothing changes if the call fails.
     *
     * @throws InvalidUserDataException if the imported build was already read, [spec] excludes a target the file
     * does not add, or a task name is taken
     * @throws com.kelvsyc.gradle.ant.AntExecutionException if Ant fails to read the build
     */
    fun importBuild(file: File, baseDirectory: File?, spec: AntImportSpec): AntImport {
        if (frozen) {
            throw InvalidUserDataException(
                "Cannot import $file: importBuild after the imported Ant build was read (by a task, or through " +
                    "propertiesFor). Import every build file before reading it."
            )
        }
        val classpath = spec.antClasspath ?: defaultClasspath
        val newFiles = files + AntShim.BuildFile(file, baseDirectory)
        val newSeeds = LinkedHashMap(seedValues)
        spec.resolveSeeds().forEach { (key, value) -> newSeeds.putIfAbsent(key, value) }
        val model = AntRuntimeClassLoader(classpath.files.toList()).use {
            AntShim(it).readBuild(newFiles, newSeeds, logger)
        }
        val added = model.targets.map { it.name }.filter { it !in targets }
        val names = taskNamesFor(file, added, spec)

        files.clear()
        files += newFiles
        seedValues.clear()
        seedValues += newSeeds
        targets = model.targets.associateBy { it.name }
        taskNames += names
        topLevelPropertyValues = LinkedHashMap(model.topLevelProperties)
        buildFileValues = (model.loadedFiles + model.readFiles).distinct()

        names.forEach { (target, name) ->
            if (spec.exclusions.containsKey(target)) {
                exclusions[target] = spec.exclusions[target]?.supplied?.let { registerExport(name, it) }
            } else {
                registerTarget(target, name, spec, classpath)
            }
        }
        // Configuration-cache inputs: editing any file of the build invalidates the entry.
        buildFileValues.forEach { loaded ->
            providers.fileContents(layout.file(providers.provider { loaded })).asBytes.get()
        }
        return AntImport(this)
    }

    /**
     * See [AntImport.propertiesFor].
     */
    fun propertiesFor(target: String): Provider<Map<String, String>> {
        val base = seeds.zip(topLevelProperties, ::firstWins)
        return providers.provider { upstreamExportsOf(target) }.flatMap { exports ->
            exports.fold(base) { merged, export ->
                merged.zip(export.map { PropertiesFiles.read(it.asFile) }, ::firstWins)
            }
        }
    }

    /**
     * Logs a warning for each excluded target that has imported targets downstream and no supplied properties.
     */
    fun warnAboutUnsuppliedExclusions() {
        exclusions.filterValues { it == null }.keys.forEach { excluded ->
            val downstream = taskNames.keys.filter { it !in exclusions && excluded in targets.getValue(it).upstream }
            if (downstream.isNotEmpty()) {
                logger.warn(
                    "Excluded Ant target '$excluded' has imported targets downstream (${downstream.joinToString()}) " +
                        "and its replacement supplies no properties: properties it used to define are undefined " +
                        "downstream unless supplied with exclude(\"$excluded\") { properties(...) }."
                )
            }
        }
    }

    private fun taskNamesFor(file: File, added: List<String>, spec: AntImportSpec): Map<String, String> {
        val unknown = spec.exclusions.keys - added.toSet()
        if (unknown.isNotEmpty()) {
            throw InvalidUserDataException(
                "Cannot exclude ${unknown.joinToString { "'$it'" }}: not a target that importing $file adds."
            )
        }
        val names = added.associateWith { spec.taskNameTransformer.transform(it) }
        // Every task the call registers: an imported target's, or the export task of an exclusion supplying properties.
        val registered = names.mapNotNull { (target, name) ->
            when {
                target !in spec.exclusions -> name to target
                spec.exclusions[target]?.supplied != null -> exportTaskName(name) to target
                else -> null
            }
        }
        val clashes = registered.groupBy({ it.first }, { it.second })
            .filter { (name, targets) -> targets.size > 1 || name in tasks.names }
        if (clashes.isNotEmpty()) {
            val described = clashes.entries.joinToString { (name, targets) ->
                "task '$name' (Ant target${if (targets.size > 1) "s" else ""} ${targets.joinToString { "'$it'" }})"
            }
            throw InvalidUserDataException(
                "Cannot import $file: task names clash with an existing task or with each other: $described. " +
                    "Rename the imported tasks with taskName { ... }."
            )
        }
        return names
    }

    private fun registerTarget(target: String, name: String, spec: AntImportSpec, classpath: FileCollection) {
        val targetDescription = targets.getValue(target).description
        val targetIsolation = spec.isolation
        tasks.register(name, AntImportedTarget::class.java) {
            this.target.set(target)
            targetDescription?.let { description = it }
            buildFiles.from(this@AntImportRegistry.buildFiles)
            topLevelBuildFiles.set(topLevelFiles)
            baseDirectories.set(this@AntImportRegistry.baseDirectories)
            seeds.set(this@AntImportRegistry.seeds)
            upstreamExports.addAll(
                providers.provider { upstreamExportsOf(target) }.flatMap { exports ->
                    objects.listProperty(RegularFile::class.java).apply { exports.forEach { add(it) } }
                }
            )
            dependsOn(providers.provider { dependencyNames(target) })
            antClasspath.from(classpath)
            isolation.set(targetIsolation)
        }
    }

    private fun registerExport(name: String, supplied: Provider<Map<String, String>>) =
        tasks.register(exportTaskName(name), AntExclusionExport::class.java) {
            description = "Writes the properties supplied in place of the excluded Ant target '$name'."
            properties.set(supplied)
            exportFile.set(layout.buildDirectory.file("ant-bridge/import/${this.name}.properties"))
        }

    private fun <T : Any> frozenProvider(value: () -> T): Provider<T> =
        providers.provider {
            frozen = true
            value()
        }

    private fun target(name: String): AntShim.BuildTarget =
        targets[name] ?: throw InvalidUserDataException("No Ant target '$name' in the imported Ant build.")

    private fun dependencyNames(target: String): List<String> =
        target(target).depends.distinct().map { taskNames[it] ?: it }

    /** The export files the upstream targets of [target] offer, in Ant's order. */
    private fun upstreamExportsOf(target: String): List<Provider<RegularFile>> =
        target(target).upstream.mapNotNull { upstream ->
            when (upstream) {
                in exclusions -> exclusions[upstream]?.flatMap { it.exportFile }
                in taskNames -> tasks.named(taskNames.getValue(upstream), AntImportedTarget::class.java)
                    .flatMap { it.exportFile }
                else -> null
            }
        }
}

/** The name of the task writing the properties supplied for the excluded target whose task name is [name]. */
private fun exportTaskName(name: String): String = "${name}AntProperties"

/** Merges [second] into [first], the first definition of a name winning. */
private fun firstWins(first: Map<String, String>, second: Map<String, String>): Map<String, String> {
    val result = LinkedHashMap(first)
    second.forEach { (key, value) -> result.putIfAbsent(key, value) }
    return result
}

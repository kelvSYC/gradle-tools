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

    private val topLevelFiles = objects.listProperty(File::class.java).apply { finalizeValueOnRead() }
    private val baseDirectories = objects.listProperty(String::class.java).apply { finalizeValueOnRead() }
    private val seeds = objects.mapProperty(String::class.java, String::class.java).apply { finalizeValueOnRead() }
    private val topLevelProperties =
        objects.mapProperty(String::class.java, String::class.java).apply { finalizeValueOnRead() }
    private val loadedFiles = objects.fileCollection().apply { finalizeValueOnRead() }

    /**
     * Adds [file] (based at [baseDirectory], if given) to the build, re-reads the whole build, and registers a task
     * for every target the file adds, except the targets [spec] excludes.
     *
     * @throws InvalidUserDataException if [spec] excludes a target the file does not add, or a task name is taken
     * @throws com.kelvsyc.gradle.ant.AntExecutionException if Ant fails to read the build
     */
    fun importBuild(file: File, baseDirectory: File?, spec: AntImportSpec): AntImport {
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
        topLevelFiles.set(files.map { it.file })
        baseDirectories.set(files.map { it.baseDirectory?.absolutePath.orEmpty() })
        seeds.set(seedValues)
        topLevelProperties.set(model.topLevelProperties)
        loadedFiles.setFrom(model.loadedFiles)

        names.forEach { (target, name) ->
            if (spec.exclusions.containsKey(target)) {
                exclusions[target] = spec.exclusions[target]?.supplied?.let { registerExport(name, it) }
            } else {
                registerTarget(target, name, spec, classpath)
            }
        }
        // Configuration-cache inputs: editing any file of the build invalidates the entry.
        model.loadedFiles.distinct().forEach { loaded ->
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
        val clashes = names.filter { (target, name) -> target !in spec.exclusions && name in tasks.names }
        if (clashes.isNotEmpty()) {
            val described = clashes.entries.joinToString { (target, name) -> "Ant target '$target' (task '$name')" }
            throw InvalidUserDataException(
                "Cannot import $file: a task with the same name already exists for $described. " +
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
            buildFiles.from(loadedFiles)
            topLevelBuildFiles.set(topLevelFiles)
            baseDirectories.set(this@AntImportRegistry.baseDirectories)
            seeds.set(this@AntImportRegistry.seeds)
            upstreamExports.addAll(upstreamExportFiles(target))
            dependsOn(providers.provider { dependencyNames(target) })
            antClasspath.from(classpath)
            isolation.set(targetIsolation)
        }
    }

    private fun registerExport(name: String, supplied: Provider<Map<String, String>>) =
        tasks.register("${name}AntProperties", AntExclusionExport::class.java) {
            description = "Writes the properties supplied in place of the excluded Ant target '$name'."
            properties.set(supplied)
            exportFile.set(layout.buildDirectory.file("ant-bridge/import/${this.name}.properties"))
        }

    private fun target(name: String): AntShim.BuildTarget =
        targets[name] ?: throw InvalidUserDataException("No Ant target '$name' in the imported Ant build.")

    private fun dependencyNames(target: String): List<String> =
        target(target).depends.distinct().map { taskNames[it] ?: it }

    private fun upstreamExportFiles(target: String): Provider<List<RegularFile>> =
        providers.provider { upstreamExportsOf(target) }.flatMap { exports ->
            objects.listProperty(RegularFile::class.java).apply { exports.forEach { add(it) } }
        }

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

/** Merges [second] into [first], the first definition of a name winning. */
private fun firstWins(first: Map<String, String>, second: Map<String, String>): Map<String, String> {
    val result = LinkedHashMap(first)
    second.forEach { (key, value) -> result.putIfAbsent(key, value) }
    return result
}

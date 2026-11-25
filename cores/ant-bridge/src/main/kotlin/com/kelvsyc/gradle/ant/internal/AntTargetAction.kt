package com.kelvsyc.gradle.ant.internal

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.io.File

/**
 * Runs one target of an imported Ant build in a fresh Ant project, in an isolated class loader created and closed per
 * execution. The properties of the upstream targets' export files are loaded first, and the properties the target
 * newly defines are written to [Parameters.exportFile]. Any previous export file is deleted before the run, so a
 * failing target leaves none. Not designed to be extended.
 *
 * @throws com.kelvsyc.gradle.ant.AntExecutionException if Ant reports a failure
 */
abstract class AntTargetAction : WorkAction<AntTargetAction.Parameters> {
    /**
     * Parameters of [AntTargetAction].
     */
    interface Parameters : WorkParameters {
        /** The top-level build files to parse, in order. */
        val buildFiles: ListProperty<File>

        /** The base directory of each of [buildFiles], in the same order; an empty string means none is set. */
        val baseDirectories: ListProperty<String>

        /** Properties applied before the build files are parsed. */
        val seeds: MapProperty<String, String>

        /** The export files of the upstream targets, in the order to load them. */
        val upstreamExports: ListProperty<RegularFile>

        /** The name of the target to run. */
        val target: Property<String>

        /** The properties file to write the target's new properties to (an empty file if there are none). */
        val exportFile: RegularFileProperty

        /** The Ant classpath: Ant, its antlibs and anything they need. */
        val antClasspath: ConfigurableFileCollection
    }

    override fun execute() {
        val logger = Logging.getLogger(AntTargetAction::class.java)
        val target = parameters.target.get()
        val export = parameters.exportFile.get().asFile
        export.delete()
        val files = parameters.buildFiles.get().zip(parameters.baseDirectories.get()) { file, base ->
            AntShim.BuildFile(file, base.ifEmpty { null }?.let(::File))
        }
        logger.info("Running Ant target {} in process {}", target, ProcessHandle.current().pid())
        AntRuntimeClassLoader(parameters.antClasspath.files.toList()).use { loader ->
            val properties = AntShim(loader).runTarget(
                files,
                parameters.seeds.getOrElse(emptyMap()),
                parameters.upstreamExports.get().map { PropertiesFiles.read(it.asFile) },
                target,
                logger
            )
            PropertiesFiles.write(export, properties)
        }
    }
}

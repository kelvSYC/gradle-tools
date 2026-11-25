package com.kelvsyc.gradle.ant.actions

import com.kelvsyc.gradle.ant.AntNode
import com.kelvsyc.gradle.ant.internal.AntRuntimeClassLoader
import com.kelvsyc.gradle.ant.internal.AntShim
import com.kelvsyc.gradle.ant.internal.PropertiesFiles
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.SetProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Runs a resolved Ant tree in a fresh Ant project, in an isolated class loader holding Ant and its antlibs, which is
 * created and closed per execution. Ant's messages go to Gradle's logger at matching levels.
 *
 * Submit it to any work queue, next to other work items: the action isolates Ant itself, so the queue's isolation
 * mode only decides where it runs (in the daemon or in a worker process), and the worker classpath needs nothing.
 * Build [Parameters.nodes] with [com.kelvsyc.gradle.ant.AntTrees] and [com.kelvsyc.gradle.ant.AntElement.resolve] at
 * execution time, and set [Parameters.antClasspath] from the `antBridge` extension's `classpath`. Not designed to be
 * extended.
 *
 * @throws com.kelvsyc.gradle.ant.AntExecutionException if Ant reports a failure
 */
abstract class AntWorkAction : WorkAction<AntWorkAction.Parameters> {
    /**
     * Parameters of [AntWorkAction].
     */
    interface Parameters : WorkParameters {
        /** The Ant tasks to run, in order, resolved. */
        val nodes: ListProperty<AntNode>

        /** The Ant classpath: Ant, its antlibs and anything they need. */
        val antClasspath: ConfigurableFileCollection

        /** The Ant project's base directory (`basedir`). */
        val baseDirectory: DirectoryProperty

        /** Names of Ant properties whose values to report after the run; those the tree did not set are omitted. */
        val resultProperties: SetProperty<String>

        /**
         * The properties file to write the values of [resultProperties] to (an empty file if none is set). Nothing is
         * written when absent.
         */
        val resultFile: RegularFileProperty
    }

    override fun execute() {
        val logger = Logging.getLogger(AntWorkAction::class.java)
        AntRuntimeClassLoader(parameters.antClasspath.files.toList()).use { loader ->
            val result = AntShim(loader).execute(
                parameters.nodes.get(),
                parameters.baseDirectory.get().asFile,
                logger,
                parameters.resultProperties.getOrElse(emptySet())
            )
            logger.info("Running Ant {} in process {}", result.antVersion, ProcessHandle.current().pid())
            if (parameters.resultFile.isPresent) {
                PropertiesFiles.write(parameters.resultFile.get().asFile, result.properties)
            }
        }
    }
}

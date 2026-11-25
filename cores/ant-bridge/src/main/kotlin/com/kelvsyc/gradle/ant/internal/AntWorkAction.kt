package com.kelvsyc.gradle.ant.internal

import com.kelvsyc.gradle.ant.AntNode
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Runs a resolved Ant tree in a fresh Ant project, in an isolated class loader holding Ant and its antlibs, which is
 * created and closed per execution. Ant's messages go to Gradle's logger at matching levels.
 *
 * @throws com.kelvsyc.gradle.ant.AntExecutionException if Ant reports a failure
 */
abstract class AntWorkAction : WorkAction<AntWorkAction.Parameters> {
    /**
     * Parameters of [AntWorkAction].
     */
    interface Parameters : WorkParameters {
        /** The Ant tasks to run, in order. */
        val nodes: ListProperty<AntNode>

        /** The Ant classpath: Ant, its antlibs and anything they need. */
        val antClasspath: ConfigurableFileCollection

        /** The Ant project's base directory (`basedir`). */
        val baseDirectory: DirectoryProperty
    }

    override fun execute() {
        val logger = Logging.getLogger(AntWorkAction::class.java)
        AntRuntimeClassLoader(parameters.antClasspath.files.toList()).use { loader ->
            val version = AntShim(loader).execute(
                parameters.nodes.get(),
                parameters.baseDirectory.get().asFile,
                logger
            )
            logger.info("Running Ant {} in process {}", version, ProcessHandle.current().pid())
        }
    }
}

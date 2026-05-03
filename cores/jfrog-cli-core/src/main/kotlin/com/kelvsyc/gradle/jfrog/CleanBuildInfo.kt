package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.jfrog.actions.CleanBuildInfoAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task deleting the accumulated local build info, using the JFrog CLI.
 *
 * This task runs `jf rt build-clean` and does not require a server connection. It removes
 * the locally accumulated build info files without affecting any already-published build info
 * on the Artifactory server.
 *
 * The implementation of this task delegates to [CleanBuildInfoAction].
 */
@DisableCachingByDefault(because = "Build info cleanup is not cacheable")
abstract class CleanBuildInfo @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The underlying JFrog CLI command.
     *
     * @see [CleanBuildInfoAction.Parameters.jfCommand]
     */
    @get:Internal
    abstract val jfCommand: Property<String>

    /**
     * The build name.
     *
     * @see [CleanBuildInfoAction.Parameters.buildName]
     */
    @get:Input
    abstract val buildName: Property<String>

    /**
     * The build number.
     *
     * @see [CleanBuildInfoAction.Parameters.buildNumber]
     */
    @get:Input
    abstract val buildNumber: Property<String>

    @TaskAction
    fun run() {
        workers.noIsolation().submit(CleanBuildInfoAction::class) {
            jfCommand.set(this@CleanBuildInfo.jfCommand)
            buildName.set(this@CleanBuildInfo.buildName)
            buildNumber.set(this@CleanBuildInfo.buildNumber)
        }
    }
}

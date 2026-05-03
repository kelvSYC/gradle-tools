package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.jfrog.actions.CollectBuildEnvironmentAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task collecting environment variables into the accumulated local build info, using the JFrog CLI.
 *
 * This task runs `jf rt build-collect-env` and does not require a server connection. The accumulated
 * build info is published to Artifactory by a subsequent [PublishBuildInfo] task.
 *
 * The implementation of this task delegates to [CollectBuildEnvironmentAction].
 */
@DisableCachingByDefault(because = "Build info accumulation is not cacheable")
abstract class CollectBuildEnvironment @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The underlying JFrog CLI command.
     *
     * @see [CollectBuildEnvironmentAction.Parameters.jfCommand]
     */
    @get:Internal
    abstract val jfCommand: Property<String>

    /**
     * The build name.
     *
     * @see [CollectBuildEnvironmentAction.Parameters.buildName]
     */
    @get:Input
    abstract val buildName: Property<String>

    /**
     * The build number.
     *
     * @see [CollectBuildEnvironmentAction.Parameters.buildNumber]
     */
    @get:Input
    abstract val buildNumber: Property<String>

    /**
     * Glob patterns for environment variable names to include. Leave empty to include all variables.
     *
     * @see [CollectBuildEnvironmentAction.Parameters.includePatterns]
     */
    @get:Input
    @get:Optional
    abstract val includePatterns: ListProperty<String>

    /**
     * Glob patterns for environment variable names to exclude.
     *
     * @see [CollectBuildEnvironmentAction.Parameters.excludePatterns]
     */
    @get:Input
    @get:Optional
    abstract val excludePatterns: ListProperty<String>

    @TaskAction
    fun run() {
        workers.noIsolation().submit(CollectBuildEnvironmentAction::class) {
            jfCommand.set(this@CollectBuildEnvironment.jfCommand)
            buildName.set(this@CollectBuildEnvironment.buildName)
            buildNumber.set(this@CollectBuildEnvironment.buildNumber)
            includePatterns.set(this@CollectBuildEnvironment.includePatterns)
            excludePatterns.set(this@CollectBuildEnvironment.excludePatterns)
        }
    }
}

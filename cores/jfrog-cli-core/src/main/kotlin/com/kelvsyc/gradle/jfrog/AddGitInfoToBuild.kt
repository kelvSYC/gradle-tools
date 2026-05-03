package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.jfrog.actions.AddGitInfoToBuildAction
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
 * Task adding Git VCS information to the accumulated local build info, using the JFrog CLI.
 *
 * This task runs `jf rt build-add-git` and does not require a server connection. The accumulated
 * build info is published to Artifactory by a subsequent [PublishBuildInfo] task.
 *
 * The implementation of this task delegates to [AddGitInfoToBuildAction].
 */
@DisableCachingByDefault(because = "Build info accumulation is not cacheable")
abstract class AddGitInfoToBuild @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The underlying JFrog CLI command.
     *
     * @see [AddGitInfoToBuildAction.Parameters.jfCommand]
     */
    @get:Internal
    abstract val jfCommand: Property<String>

    /**
     * The build name.
     *
     * @see [AddGitInfoToBuildAction.Parameters.buildName]
     */
    @get:Input
    abstract val buildName: Property<String>

    /**
     * The build number.
     *
     * @see [AddGitInfoToBuildAction.Parameters.buildNumber]
     */
    @get:Input
    abstract val buildNumber: Property<String>

    @TaskAction
    fun run() {
        workers.noIsolation().submit(AddGitInfoToBuildAction::class) {
            jfCommand.set(this@AddGitInfoToBuild.jfCommand)
            buildName.set(this@AddGitInfoToBuild.buildName)
            buildNumber.set(this@AddGitInfoToBuild.buildNumber)
        }
    }
}

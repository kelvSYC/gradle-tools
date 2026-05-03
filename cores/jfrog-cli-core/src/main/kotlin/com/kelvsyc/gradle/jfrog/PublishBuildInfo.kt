package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.jfrog.actions.PublishBuildInfoAction
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
 * Task publishing accumulated local build info to Artifactory, using the JFrog CLI.
 *
 * This task runs `jf rt build-publish` and requires a server connection. Build info is typically
 * accumulated beforehand by [AddGitInfoToBuild] and [CollectBuildEnvironment].
 *
 * The implementation of this task delegates to [PublishBuildInfoAction].
 */
@DisableCachingByDefault(because = "Publishing to Artifactory is not cacheable")
abstract class PublishBuildInfo @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The underlying JFrog CLI command.
     *
     * @see [PublishBuildInfoAction.Parameters.jfCommand]
     */
    @get:Internal
    abstract val jfCommand: Property<String>

    /**
     * The Artifactory server URL.
     *
     * @see [PublishBuildInfoAction.Parameters.serverUrl]
     */
    @get:Input
    abstract val serverUrl: Property<String>

    /**
     * The JFrog access token for authentication.
     *
     * @see [PublishBuildInfoAction.Parameters.accessToken]
     */
    @get:Internal
    abstract val accessToken: Property<String>

    /**
     * The build name.
     *
     * @see [PublishBuildInfoAction.Parameters.buildName]
     */
    @get:Input
    abstract val buildName: Property<String>

    /**
     * The build number.
     *
     * @see [PublishBuildInfoAction.Parameters.buildNumber]
     */
    @get:Input
    abstract val buildNumber: Property<String>

    /**
     * Additional environment variable name patterns to exclude from the published build info.
     *
     * @see [PublishBuildInfoAction.Parameters.envExcludePatterns]
     */
    @get:Input
    @get:Optional
    abstract val envExcludePatterns: ListProperty<String>

    @TaskAction
    fun run() {
        workers.noIsolation().submit(PublishBuildInfoAction::class) {
            jfCommand.set(this@PublishBuildInfo.jfCommand)
            serverUrl.set(this@PublishBuildInfo.serverUrl)
            accessToken.set(this@PublishBuildInfo.accessToken)
            buildName.set(this@PublishBuildInfo.buildName)
            buildNumber.set(this@PublishBuildInfo.buildNumber)
            envExcludePatterns.set(this@PublishBuildInfo.envExcludePatterns)
        }
    }
}

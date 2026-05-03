package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.jfrog.actions.ScanBuildAction
import org.gradle.api.DefaultTask
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
 * Task triggering an Xray security scan of a published build, using the JFrog CLI.
 *
 * This task runs `jf rt build-scan` and requires a server connection with Xray enabled. The build
 * must already be published to Artifactory before scanning; use [PublishBuildInfo] first.
 *
 * The implementation of this task delegates to [ScanBuildAction].
 */
@DisableCachingByDefault(because = "Xray scanning results are not cacheable")
abstract class ScanBuild @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    /**
     * The underlying JFrog CLI command.
     *
     * @see [ScanBuildAction.Parameters.jfCommand]
     */
    @get:Internal
    abstract val jfCommand: Property<String>

    /**
     * The Artifactory server URL.
     *
     * @see [ScanBuildAction.Parameters.serverUrl]
     */
    @get:Input
    abstract val serverUrl: Property<String>

    /**
     * The JFrog access token for authentication.
     *
     * @see [ScanBuildAction.Parameters.accessToken]
     */
    @get:Internal
    abstract val accessToken: Property<String>

    /**
     * The build name.
     *
     * @see [ScanBuildAction.Parameters.buildName]
     */
    @get:Input
    abstract val buildName: Property<String>

    /**
     * The build number.
     *
     * @see [ScanBuildAction.Parameters.buildNumber]
     */
    @get:Input
    abstract val buildNumber: Property<String>

    /**
     * Whether to fail the build if Xray policy violations are found. Defaults to `false`.
     *
     * @see [ScanBuildAction.Parameters.failBuild]
     */
    @get:Input
    @get:Optional
    abstract val failBuild: Property<Boolean>

    @TaskAction
    fun run() {
        workers.noIsolation().submit(ScanBuildAction::class) {
            jfCommand.set(this@ScanBuild.jfCommand)
            serverUrl.set(this@ScanBuild.serverUrl)
            accessToken.set(this@ScanBuild.accessToken)
            buildName.set(this@ScanBuild.buildName)
            buildNumber.set(this@ScanBuild.buildNumber)
            failBuild.set(this@ScanBuild.failBuild)
        }
    }
}

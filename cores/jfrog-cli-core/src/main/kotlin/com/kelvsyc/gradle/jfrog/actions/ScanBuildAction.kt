package com.kelvsyc.gradle.jfrog.actions

import com.kelvsyc.gradle.jfrog.which
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * [WorkAction] implementation triggering an Xray security scan of a published build using the JFrog CLI.
 */
abstract class ScanBuildAction @Inject constructor(
    private val exec: ExecOperations, private val providers: ProviderFactory
) : WorkAction<ScanBuildAction.Parameters> {
    /**
     * Parameters for [ScanBuildAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The underlying JFrog CLI command.
         */
        val jfCommand: Property<String>

        /**
         * The Artifactory server URL.
         */
        val serverUrl: Property<String>

        /**
         * The JFrog access token for authentication.
         */
        val accessToken: Property<String>

        /**
         * The build name.
         */
        val buildName: Property<String>

        /**
         * The build number.
         */
        val buildNumber: Property<String>

        /**
         * Whether to fail the build if Xray policy violations are found. Defaults to `false`.
         */
        val failBuild: Property<Boolean>
    }

    private val jfCommandInternal = parameters.jfCommand.orElse(providers.which("jf"))

    override fun execute() {
        val args = buildList {
            add("rt")
            add("build-scan")
            add("--url")
            add(parameters.serverUrl.get())
            add("--access-token")
            add(parameters.accessToken.get())
            if (parameters.failBuild.getOrElse(false)) {
                add("--fail")
            }
            add(parameters.buildName.get())
            add(parameters.buildNumber.get())
        }
        exec.exec {
            executable(jfCommandInternal.get())
            args(args)
        }
    }
}

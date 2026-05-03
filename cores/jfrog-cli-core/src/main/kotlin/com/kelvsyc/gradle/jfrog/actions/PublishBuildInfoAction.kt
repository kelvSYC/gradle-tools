package com.kelvsyc.gradle.jfrog.actions

import com.kelvsyc.gradle.jfrog.which
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * [WorkAction] implementation publishing accumulated local build info to Artifactory using the JFrog CLI.
 */
abstract class PublishBuildInfoAction @Inject constructor(
    private val exec: ExecOperations, private val providers: ProviderFactory
) : WorkAction<PublishBuildInfoAction.Parameters> {
    /**
     * Parameters for [PublishBuildInfoAction].
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
         * Additional environment variable name patterns to exclude from the published build info.
         */
        val envExcludePatterns: ListProperty<String>
    }

    private val jfCommandInternal = parameters.jfCommand.orElse(providers.which("jf"))

    override fun execute() {
        val args = buildList {
            add("rt")
            add("build-publish")
            add("--url")
            add(parameters.serverUrl.get())
            add("--access-token")
            add(parameters.accessToken.get())
            val excludes = parameters.envExcludePatterns.getOrElse(emptyList())
            if (excludes.isNotEmpty()) {
                add("--env-exclude")
                add(excludes.joinToString(";"))
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

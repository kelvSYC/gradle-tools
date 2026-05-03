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
 * [WorkAction] implementation collecting environment variables into the accumulated local build info using the JFrog CLI.
 */
abstract class CollectBuildEnvironmentAction @Inject constructor(
    private val exec: ExecOperations, private val providers: ProviderFactory
) : WorkAction<CollectBuildEnvironmentAction.Parameters> {
    /**
     * Parameters for [CollectBuildEnvironmentAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The underlying JFrog CLI command.
         */
        val jfCommand: Property<String>

        /**
         * The build name.
         */
        val buildName: Property<String>

        /**
         * The build number.
         */
        val buildNumber: Property<String>

        /**
         * Glob patterns for environment variable names to include. Leave empty to include all variables.
         */
        val includePatterns: ListProperty<String>

        /**
         * Glob patterns for environment variable names to exclude.
         */
        val excludePatterns: ListProperty<String>
    }

    private val jfCommandInternal = parameters.jfCommand.orElse(providers.which("jf"))

    override fun execute() {
        val args = buildList {
            add("rt")
            add("build-collect-env")
            val includes = parameters.includePatterns.getOrElse(emptyList())
            if (includes.isNotEmpty()) {
                add("--include-vars")
                add(includes.joinToString(";"))
            }
            val excludes = parameters.excludePatterns.getOrElse(emptyList())
            if (excludes.isNotEmpty()) {
                add("--exclude-vars")
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

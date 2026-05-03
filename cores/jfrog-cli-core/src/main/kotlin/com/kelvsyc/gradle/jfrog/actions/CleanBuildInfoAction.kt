package com.kelvsyc.gradle.jfrog.actions

import com.kelvsyc.gradle.jfrog.which
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import javax.inject.Inject

/**
 * [WorkAction] implementation deleting the accumulated local build info using the JFrog CLI.
 */
abstract class CleanBuildInfoAction @Inject constructor(
    private val exec: ExecOperations, private val providers: ProviderFactory
) : WorkAction<CleanBuildInfoAction.Parameters> {
    /**
     * Parameters for [CleanBuildInfoAction].
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
    }

    private val jfCommandInternal = parameters.jfCommand.orElse(providers.which("jf"))

    override fun execute() {
        exec.exec {
            executable(jfCommandInternal.get())
            args(listOf("rt", "build-clean", parameters.buildName.get(), parameters.buildNumber.get()))
        }
    }
}

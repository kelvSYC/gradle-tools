package com.kelvsyc.gradle.azure.containerapp.actions

import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that starts a manual execution of a Container App Job and writes the resulting
 * execution name to [executionNameFile].
 *
 * The execution name can be consumed by downstream tasks (e.g. a polling task) via
 * [StartJobExecutionTask.executionNameFile].
 */
abstract class StartJobExecutionAction : WorkAction<StartJobExecutionAction.Parameters> {

    /**
     * Parameters for [StartJobExecutionAction].
     */
    interface Parameters : WorkParameters {
        /** The scoped service identifying which job to start. */
        @get:Internal
        val service: Property<ContainerAppJobBuildService>

        /** File to write the resulting execution name into (one line, no newline). */
        @get:Internal
        val executionNameFile: RegularFileProperty
    }

    override fun execute() {
        val jobService = parameters.service.get()
        val envService = jobService.parameters.environmentService.get()
        val manager = envService.getClient()
        val rg = envService.parameters.resourceGroupName.get()
        val jobName = jobService.parameters.jobName.get()

        val executionBase = manager.jobs().start(rg, jobName)
        parameters.executionNameFile.get().asFile.writeText(executionBase.name())
    }
}

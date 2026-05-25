package com.kelvsyc.gradle.azure.containerapp.actions

import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that cancels a running Container App Job execution.
 *
 * Has no effect if the execution has already completed or failed.
 */
abstract class StopJobExecutionAction : WorkAction<StopJobExecutionAction.Parameters> {

    /**
     * Parameters for [StopJobExecutionAction].
     */
    interface Parameters : WorkParameters {
        /** The scoped service identifying which job owns the execution. */
        @get:Internal
        val service: Property<ContainerAppJobBuildService>

        /** Full execution name to stop (e.g. `my-job--exec-abc123`). */
        val executionName: Property<String>
    }

    override fun execute() {
        val jobService = parameters.service.get()
        val envService = jobService.parameters.environmentService.get()
        envService.getClient().jobs()
            .stopExecution(
                envService.parameters.resourceGroupName.get(),
                jobService.parameters.jobName.get(),
                parameters.executionName.get(),
            )
    }
}

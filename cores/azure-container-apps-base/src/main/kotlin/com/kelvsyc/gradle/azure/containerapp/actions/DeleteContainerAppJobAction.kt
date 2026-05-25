package com.kelvsyc.gradle.azure.containerapp.actions

import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that deletes a Container App Job definition.
 *
 * Active executions of the job are not affected. The resource group is read from the parent
 * environment service; the job name is read from the [ContainerAppJobBuildService] parameters.
 */
abstract class DeleteContainerAppJobAction : WorkAction<DeleteContainerAppJobAction.Parameters> {

    /**
     * Parameters for [DeleteContainerAppJobAction].
     */
    interface Parameters : WorkParameters {
        /** The scoped service identifying which job to delete. */
        @get:Internal
        val service: Property<ContainerAppJobBuildService>
    }

    override fun execute() {
        val jobService = parameters.service.get()
        val envService = jobService.parameters.environmentService.get()
        envService.getClient().jobs()
            .deleteByResourceGroup(
                envService.parameters.resourceGroupName.get(),
                jobService.parameters.jobName.get(),
            )
    }
}

package com.kelvsyc.gradle.azure.containerapp.actions

import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that deletes an Azure Container Apps managed environment.
 *
 * The environment name and resource group are read from the [service] parameters. Deletion fails
 * if the environment still contains container apps or jobs.
 */
abstract class DeleteManagedEnvironmentAction : WorkAction<DeleteManagedEnvironmentAction.Parameters> {

    /**
     * Parameters for [DeleteManagedEnvironmentAction].
     */
    interface Parameters : WorkParameters {
        /** The environment service identifying which environment to delete. */
        @get:Internal
        val service: Property<ContainerAppsEnvironmentBuildService>
    }

    override fun execute() {
        val svc = parameters.service.get()
        svc.getClient().managedEnvironments()
            .deleteByResourceGroup(
                svc.parameters.resourceGroupName.get(),
                svc.parameters.environmentName.get(),
            )
    }
}

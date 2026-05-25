package com.kelvsyc.gradle.azure.containerapp.actions

import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that deletes a Container App.
 *
 * The resource group is read from the parent environment service's parameters. The app name is
 * read from the [ContainerAppBuildService] parameters.
 */
abstract class DeleteContainerAppAction : WorkAction<DeleteContainerAppAction.Parameters> {

    /**
     * Parameters for [DeleteContainerAppAction].
     */
    interface Parameters : WorkParameters {
        /** The scoped service identifying which app to delete. */
        @get:Internal
        val service: Property<ContainerAppBuildService>
    }

    override fun execute() {
        val appService = parameters.service.get()
        val envService = appService.parameters.environmentService.get()
        envService.getClient().containerApps()
            .deleteByResourceGroup(
                envService.parameters.resourceGroupName.get(),
                appService.parameters.containerAppName.get(),
            )
    }
}

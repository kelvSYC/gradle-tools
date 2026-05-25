package com.kelvsyc.gradle.azure.containerapp.actions

import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that activates a specific revision of a Container App.
 *
 * Activating a revision is meaningful in multi-revision mode. In single-revision mode Azure
 * manages activation automatically and this action has no effect.
 */
abstract class ActivateRevisionAction : WorkAction<ActivateRevisionAction.Parameters> {

    /**
     * Parameters for [ActivateRevisionAction].
     */
    interface Parameters : WorkParameters {
        /** The scoped service identifying which app's revision to activate. */
        @get:Internal
        val service: Property<ContainerAppBuildService>

        /** Full revision name (e.g. `my-app--abc123`). */
        val revisionName: Property<String>
    }

    override fun execute() {
        val appService = parameters.service.get()
        val envService = appService.parameters.environmentService.get()
        envService.getClient().containerAppsRevisions()
            .activateRevision(
                envService.parameters.resourceGroupName.get(),
                appService.parameters.containerAppName.get(),
                parameters.revisionName.get(),
            )
    }
}

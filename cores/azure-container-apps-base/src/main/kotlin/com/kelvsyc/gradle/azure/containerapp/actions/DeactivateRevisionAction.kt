package com.kelvsyc.gradle.azure.containerapp.actions

import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that deactivates a specific revision of a Container App.
 *
 * In multi-revision mode, use this to take a revision out of the traffic rotation. A deactivated
 * revision can be reactivated later. Traffic weighting between multiple active revisions is a
 * deployment-platform concern and is intentionally not exposed here.
 */
abstract class DeactivateRevisionAction : WorkAction<DeactivateRevisionAction.Parameters> {

    /**
     * Parameters for [DeactivateRevisionAction].
     */
    interface Parameters : WorkParameters {
        /** The scoped service identifying which app's revision to deactivate. */
        @get:Internal
        val service: Property<ContainerAppBuildService>

        /** Full revision name (e.g. `my-app--abc123`). */
        val revisionName: Property<String>
    }

    override fun execute() {
        val appService = parameters.service.get()
        val envService = appService.parameters.environmentService.get()
        envService.getClient().containerAppsRevisions()
            .deactivateRevision(
                envService.parameters.resourceGroupName.get(),
                appService.parameters.containerAppName.get(),
                parameters.revisionName.get(),
            )
    }
}

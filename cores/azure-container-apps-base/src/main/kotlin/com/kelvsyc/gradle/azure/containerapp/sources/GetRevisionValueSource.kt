package com.kelvsyc.gradle.azure.containerapp.sources

import com.azure.core.management.exception.ManagementException
import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the running state of a specific revision.
 *
 * Returns the revision's running state (e.g., `Running`, `Inactive`), or `null`
 * if the revision does not exist or the state is unavailable.
 */
abstract class GetRevisionValueSource :
    ValueSource<String, GetRevisionValueSource.Parameters> {

    /**
     * Parameters for [GetRevisionValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the container app.
         */
        @get:Internal
        val service: Property<ContainerAppBuildService>

        /**
         * The revision name (e.g., `my-app--abc123`).
         */
        val revisionName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val appSvc = parameters.service.get()
            val envSvc = appSvc.parameters.environmentService.get()
            val rg = envSvc.parameters.resourceGroupName.get()
            val appName = appSvc.parameters.containerAppName.get()
            val revisionName = parameters.revisionName.get()
            envSvc.getClient().containerAppsRevisions()
                .getRevision(rg, appName, revisionName)
                .runningState()
                ?.toString()
        } catch (e: ManagementException) {
            logger.debug("Revision not found: ${parameters.revisionName.get()}", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetRevisionValueSource::class.java)
    }
}

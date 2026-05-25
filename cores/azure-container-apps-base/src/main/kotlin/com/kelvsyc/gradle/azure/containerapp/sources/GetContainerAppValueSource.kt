package com.kelvsyc.gradle.azure.containerapp.sources

import com.azure.core.management.exception.ManagementException
import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the ingress FQDN for a container app.
 *
 * Returns the fully qualified domain name for ingress (e.g., `my-app.eastus.azurecontainerapps.io`),
 * or `null` if ingress is not configured or the app does not exist.
 */
abstract class GetContainerAppValueSource :
    ValueSource<String, GetContainerAppValueSource.Parameters> {

    /**
     * Parameters for [GetContainerAppValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the container app.
         */
        @get:Internal
        val service: Property<ContainerAppBuildService>
    }

    override fun obtain(): String? {
        return try {
            val svc = parameters.service.get()
            svc.getClient().configuration()?.ingress()?.fqdn()
        } catch (e: ManagementException) {
            logger.debug("Container app not found or configuration unavailable", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetContainerAppValueSource::class.java)
    }
}

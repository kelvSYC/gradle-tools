package com.kelvsyc.gradle.azure.containerapp.sources

import com.azure.core.management.exception.ManagementException
import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the provisioning state of a container app job.
 *
 * Returns the job's provisioning state (e.g., `Succeeded`, `Failed`), or `null`
 * if the job does not exist or the state is unavailable.
 */
abstract class GetContainerAppJobValueSource :
    ValueSource<String, GetContainerAppJobValueSource.Parameters> {

    /**
     * Parameters for [GetContainerAppJobValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the container app job.
         */
        @get:Internal
        val service: Property<ContainerAppJobBuildService>
    }

    override fun obtain(): String? {
        return try {
            val svc = parameters.service.get()
            svc.getClient().provisioningState()?.toString()
        } catch (e: ManagementException) {
            logger.debug("Container app job not found or state unavailable", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetContainerAppJobValueSource::class.java)
    }
}

package com.kelvsyc.gradle.azure.containerapp.sources

import com.azure.core.management.exception.ManagementException
import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the status of a specific job execution.
 *
 * Returns the execution's status (e.g., `Succeeded`, `Failed`, `Running`), or `null`
 * if the execution does not exist or the status is unavailable.
 */
abstract class GetJobExecutionValueSource :
    ValueSource<String, GetJobExecutionValueSource.Parameters> {

    /**
     * Parameters for [GetJobExecutionValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the container app job.
         */
        @get:Internal
        val service: Property<ContainerAppJobBuildService>

        /**
         * The execution name (e.g., `my-job--exec-abc123`).
         */
        val executionName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val jobSvc = parameters.service.get()
            val envSvc = jobSvc.parameters.environmentService.get()
            val rg = envSvc.parameters.resourceGroupName.get()
            val jobName = jobSvc.parameters.jobName.get()
            val executionName = parameters.executionName.get()
            envSvc.getClient().jobsExecutions()
                .list(rg, jobName)
                .firstOrNull { it.name() == executionName }
                ?.status()
                ?.toString()
        } catch (e: ManagementException) {
            logger.debug("Job execution not found: ${parameters.executionName.get()}", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetJobExecutionValueSource::class.java)
    }
}

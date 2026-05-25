package com.kelvsyc.gradle.azure.containerapp.sources

import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that retrieves the list of job execution names for a container app job.
 *
 * Returns a [List] of execution names (e.g., `["my-job--exec-aaa", "my-job--exec-bbb"]`),
 * or an empty list if no executions are found.
 */
abstract class ListJobExecutionsValueSource :
    ValueSource<List<String>, ListJobExecutionsValueSource.Parameters> {

    /**
     * Parameters for [ListJobExecutionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the container app job.
         */
        @get:Internal
        val service: Property<ContainerAppJobBuildService>
    }

    override fun obtain(): List<String> {
        val jobSvc = parameters.service.get()
        val envSvc = jobSvc.parameters.environmentService.get()
        val rg = envSvc.parameters.resourceGroupName.get()
        val jobName = jobSvc.parameters.jobName.get()
        return envSvc.getClient().jobsExecutions().list(rg, jobName).map { it.name() }
    }
}

package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.models.Job
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.ServiceReference

/**
 * Build service managing a [Job] instance scoped to a single Container App Job within a managed
 * environment.
 *
 * This is a chained service — it holds no credentials. The parent
 * [ContainerAppsEnvironmentBuildService] provides the [ContainerAppsApiManager] and resource
 * group; this service adds [jobName] to scope down to a single job.
 *
 * [createClient] calls [com.azure.resourcemanager.appcontainers.fluent.JobsClient.getByResourceGroup]
 * and fails if the named job does not exist. Use [ContainerAppsEnvironmentBuildService] directly
 * (not this service) for WorkActions that create a new job.
 */
abstract class ContainerAppJobBuildService :
    AbstractClientBuildService<Job, ContainerAppJobBuildService.Params>() {

    /**
     * Configuration parameters for [ContainerAppJobBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The parent environment service. Provides the [ContainerAppsApiManager] and resource
         * group name. Credentials are managed entirely by this parent service.
         */
        @get:ServiceReference
        val environmentService: Property<ContainerAppsEnvironmentBuildService>

        /** Name of the Container App Job within the managed environment. */
        val jobName: Property<String>
    }

    override fun createClient(): Job {
        val envService = parameters.environmentService.get()
        val manager = envService.getClient()
        val rg = envService.parameters.resourceGroupName.get()
        return manager.jobs().getByResourceGroup(rg, parameters.jobName.get())
    }
}

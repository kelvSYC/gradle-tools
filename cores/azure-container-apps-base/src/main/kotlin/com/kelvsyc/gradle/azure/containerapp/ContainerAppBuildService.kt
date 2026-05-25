package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.ServiceReference

/**
 * Build service managing a [ContainerApp] instance scoped to a single container app within a
 * Container Apps managed environment.
 *
 * This is a chained service — it holds no credentials. The parent
 * [ContainerAppsEnvironmentBuildService] provides the [ContainerAppsApiManager] and resource
 * group; this service adds [containerAppName] to scope down to a single app.
 *
 * [createClient] calls [com.azure.resourcemanager.appcontainers.fluent.ContainerAppsClient.getByResourceGroup]
 * and fails if the named app does not exist. Use [ContainerAppsEnvironmentBuildService] directly
 * (not this service) for WorkActions that create a new app.
 */
abstract class ContainerAppBuildService :
    AbstractClientBuildService<ContainerApp, ContainerAppBuildService.Params>() {

    /**
     * Configuration parameters for [ContainerAppBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The parent environment service. Provides the [ContainerAppsApiManager] and resource
         * group name. Credentials are managed entirely by this parent service.
         */
        @get:ServiceReference
        val environmentService: Property<ContainerAppsEnvironmentBuildService>

        /** Name of the container app within the managed environment. */
        val containerAppName: Property<String>
    }

    override fun createClient(): ContainerApp {
        val envService = parameters.environmentService.get()
        val manager = envService.getClient()
        val rg = envService.parameters.resourceGroupName.get()
        return manager.containerApps().getByResourceGroup(rg, parameters.containerAppName.get())
    }
}

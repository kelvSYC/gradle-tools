package com.kelvsyc.gradle.azure.containerapp.actions

import com.azure.resourcemanager.appcontainers.models.AppLogsConfiguration
import com.azure.resourcemanager.appcontainers.models.LogAnalyticsConfiguration
import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import com.kelvsyc.gradle.clients.CredentialReference
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that creates an Azure Container Apps managed environment.
 *
 * The environment name is read from [ContainerAppsEnvironmentBuildService.Params.environmentName].
 * Log Analytics workspace credentials are required at environment creation time.
 */
abstract class CreateManagedEnvironmentAction : WorkAction<CreateManagedEnvironmentAction.Parameters> {

    /**
     * Parameters for [CreateManagedEnvironmentAction].
     */
    interface Parameters : WorkParameters {
        /** The environment service providing the [com.azure.resourcemanager.appcontainers.ContainerAppsApiManager]. */
        @get:Internal
        val service: Property<ContainerAppsEnvironmentBuildService>

        /** Azure region (e.g. "eastus") where the environment will be created. */
        val location: Property<String>

        /** Log Analytics workspace customer ID (GUID). */
        val logAnalyticsWorkspaceId: Property<String>

        /** Reference to the Log Analytics workspace primary or secondary shared key. */
        @get:Internal
        val logAnalyticsWorkspaceKey: Property<CredentialReference>
    }

    override fun execute() {
        val svc = parameters.service.get()
        val manager = svc.getClient()
        val rg = svc.parameters.resourceGroupName.get()
        val envName = svc.parameters.environmentName.get()

        val logsConfig = AppLogsConfiguration()
            .withDestination("log-analytics")
            .withLogAnalyticsConfiguration(
                LogAnalyticsConfiguration()
                    .withCustomerId(parameters.logAnalyticsWorkspaceId.get())
                    .withSharedKey(parameters.logAnalyticsWorkspaceKey.get().resolve()),
            )

        manager.managedEnvironments()
            .define(envName)
            .withRegion(parameters.location.get())
            .withExistingResourceGroup(rg)
            .withAppLogsConfiguration(logsConfig)
            .create()
    }
}

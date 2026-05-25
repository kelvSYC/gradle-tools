package com.kelvsyc.gradle.azure.containerapp.actions

import com.azure.resourcemanager.appcontainers.models.Configuration
import com.azure.resourcemanager.appcontainers.models.Container
import com.azure.resourcemanager.appcontainers.models.EnvironmentVar
import com.azure.resourcemanager.appcontainers.models.Ingress
import com.azure.resourcemanager.appcontainers.models.Scale
import com.azure.resourcemanager.appcontainers.models.Template
import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] that creates a new Container App within the configured managed environment.
 *
 * Always uses the environment service (not [com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService])
 * because the chained service's [com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService.createClient]
 * calls `getByResourceGroup`, which would fail for an app that does not yet exist.
 */
abstract class CreateContainerAppAction : WorkAction<CreateContainerAppAction.Parameters> {

    companion object {
        private const val DEFAULT_MIN_REPLICAS = 0
        private const val DEFAULT_MAX_REPLICAS = 10
        private const val DEFAULT_INGRESS_ENABLED = false
    }

    /**
     * Parameters for [CreateContainerAppAction].
     */
    interface Parameters : WorkParameters {
        /** The environment service. Resource group and environment name are read from its params. */
        @get:Internal
        val service: Property<ContainerAppsEnvironmentBuildService>

        /** Name of the new container app. */
        val containerAppName: Property<String>

        /** Container image URI (e.g. `myregistry.azurecr.io/myapp:1.0`). */
        val imageUri: Property<String>

        /** Azure region for the container app (e.g. `eastus`). Must match the environment region. */
        val location: Property<String>

        /** Whether to enable external HTTPS ingress. Defaults to `false` when absent. */
        val ingressEnabled: Property<Boolean>

        /** Container port to expose via ingress. Required when [ingressEnabled] is `true`. */
        val targetPort: Property<Int>

        /** Minimum replica count (0 = scale-to-zero). */
        val minReplicas: Property<Int>

        /** Maximum replica count. */
        val maxReplicas: Property<Int>

        /** Optional environment variables to set on the container. */
        val envVars: MapProperty<String, String>
    }

    override fun execute() {
        val svc = parameters.service.get()
        val manager = svc.getClient()
        val rg = svc.parameters.resourceGroupName.get()
        val envName = svc.parameters.environmentName.get()
        val envId = manager.managedEnvironments().getByResourceGroup(rg, envName).id()

        val envList = parameters.envVars.getOrElse(emptyMap()).map { (k, v) ->
            EnvironmentVar().withName(k).withValue(v)
        }

        val container = Container()
            .withName("main")
            .withImage(parameters.imageUri.get())
            .withEnv(envList)

        val scale = Scale()
            .withMinReplicas(parameters.minReplicas.getOrElse(DEFAULT_MIN_REPLICAS))
            .withMaxReplicas(parameters.maxReplicas.getOrElse(DEFAULT_MAX_REPLICAS))

        val template = Template()
            .withContainers(listOf(container))
            .withScale(scale)

        val ingress = if (parameters.ingressEnabled.getOrElse(DEFAULT_INGRESS_ENABLED)) {
            Ingress()
                .withTargetPort(parameters.targetPort.get())
                .withExternal(true)
        } else {
            null
        }

        val configuration = Configuration().withIngress(ingress)

        manager.containerApps()
            .define(parameters.containerAppName.get())
            .withRegion(parameters.location.get())
            .withExistingResourceGroup(rg)
            .withManagedEnvironmentId(envId)
            .withConfiguration(configuration)
            .withTemplate(template)
            .create()
    }
}

package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.CreateContainerAppAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Creates a new Azure Container App within the configured managed environment.
 *
 * Delegates to [CreateContainerAppAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Creating a cloud resource is not cacheable")
abstract class CreateContainerAppTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The environment service. Resource group and environment name are read from its params. */
    @get:Internal
    abstract val environmentService: Property<ContainerAppsEnvironmentBuildService>

    /** Name of the new container app. */
    @get:Input
    abstract val containerAppName: Property<String>

    /** Container image URI. */
    @get:Input
    abstract val imageUri: Property<String>

    /** Azure region. */
    @get:Input
    abstract val location: Property<String>

    /** Whether to enable external HTTPS ingress. */
    @get:Input
    @get:Optional
    abstract val ingressEnabled: Property<Boolean>

    /** Container port to expose. Required when [ingressEnabled] is `true`. */
    @get:Input
    @get:Optional
    abstract val targetPort: Property<Int>

    /** Minimum replica count. */
    @get:Input
    @get:Optional
    abstract val minReplicas: Property<Int>

    /** Maximum replica count. */
    @get:Input
    @get:Optional
    abstract val maxReplicas: Property<Int>

    /** Optional environment variables. */
    @get:Input
    @get:Optional
    abstract val envVars: MapProperty<String, String>

    /** Submits [CreateContainerAppAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(CreateContainerAppAction::class.java) { params ->
            params.service.set(environmentService)
            params.containerAppName.set(containerAppName)
            params.imageUri.set(imageUri)
            params.location.set(location)
            params.ingressEnabled.set(ingressEnabled)
            params.targetPort.set(targetPort)
            params.minReplicas.set(minReplicas)
            params.maxReplicas.set(maxReplicas)
            params.envVars.set(envVars)
        }
    }
}

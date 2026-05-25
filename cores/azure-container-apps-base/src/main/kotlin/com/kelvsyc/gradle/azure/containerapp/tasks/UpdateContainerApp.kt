package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.UpdateContainerAppAction
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
 * Updates the container image of an existing Azure Container App, triggering a new revision.
 *
 * Delegates to [UpdateContainerAppAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Deploying to an external service is not cacheable")
abstract class UpdateContainerApp @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The scoped service providing the target container app. */
    @get:Internal
    abstract val containerAppService: Property<ContainerAppBuildService>

    /** New container image URI to deploy. */
    @get:Input
    abstract val imageUri: Property<String>

    /** Replacement environment variables. When absent, existing env vars are preserved. */
    @get:Input
    @get:Optional
    abstract val envVars: MapProperty<String, String>

    /** Submits [UpdateContainerAppAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(UpdateContainerAppAction::class.java) { params ->
            params.service.set(containerAppService)
            params.imageUri.set(imageUri)
            params.envVars.set(envVars)
        }
    }
}

package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.UpdateContainerAppJobAction
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
 * Updates the container image of an existing Azure Container App Job.
 *
 * Delegates to [UpdateContainerAppJobAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Deploying to an external service is not cacheable")
abstract class UpdateContainerAppJobTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The scoped service providing the target job. */
    @get:Internal
    abstract val jobService: Property<ContainerAppJobBuildService>

    /** New container image URI. */
    @get:Input
    abstract val imageUri: Property<String>

    /** Replacement environment variables. */
    @get:Input
    @get:Optional
    abstract val envVars: MapProperty<String, String>

    /** Submits [UpdateContainerAppJobAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(UpdateContainerAppJobAction::class.java) { params ->
            params.service.set(jobService)
            params.imageUri.set(imageUri)
            params.envVars.set(envVars)
        }
    }
}

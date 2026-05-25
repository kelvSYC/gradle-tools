package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppsEnvironmentBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.DeleteManagedEnvironmentAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Deletes an Azure Container Apps managed environment.
 *
 * Delegates to [DeleteManagedEnvironmentAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Deleting a cloud resource is not cacheable")
abstract class DeleteManagedEnvironmentTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The environment service identifying which environment to delete. */
    @get:Internal
    abstract val environmentService: Property<ContainerAppsEnvironmentBuildService>

    /** Submits [DeleteManagedEnvironmentAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(DeleteManagedEnvironmentAction::class.java) { params ->
            params.service.set(environmentService)
        }
    }
}

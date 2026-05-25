package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.DeleteContainerAppJobAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Deletes an Azure Container App Job definition.
 *
 * Delegates to [DeleteContainerAppJobAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Deleting a cloud resource is not cacheable")
abstract class DeleteContainerAppJobTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The scoped service identifying which job to delete. */
    @get:Internal
    abstract val jobService: Property<ContainerAppJobBuildService>

    /** Submits [DeleteContainerAppJobAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(DeleteContainerAppJobAction::class.java) { params ->
            params.service.set(jobService)
        }
    }
}

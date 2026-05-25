package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.DeleteContainerAppAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Deletes an Azure Container App.
 *
 * Delegates to [DeleteContainerAppAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Deleting a cloud resource is not cacheable")
abstract class DeleteContainerAppTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The scoped service identifying which app to delete. */
    @get:Internal
    abstract val containerAppService: Property<ContainerAppBuildService>

    /** Submits [DeleteContainerAppAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(DeleteContainerAppAction::class.java) { params ->
            params.service.set(containerAppService)
        }
    }
}

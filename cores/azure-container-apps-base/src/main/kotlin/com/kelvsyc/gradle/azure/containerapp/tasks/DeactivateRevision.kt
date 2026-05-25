package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.DeactivateRevisionAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Deactivates a specific revision of an Azure Container App.
 *
 * Delegates to [DeactivateRevisionAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Deactivating a revision is not cacheable")
abstract class DeactivateRevision @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The scoped service identifying which app's revision to deactivate. */
    @get:Internal
    abstract val containerAppService: Property<ContainerAppBuildService>

    /** Full revision name (e.g. `my-app--abc123`). */
    @get:Input
    abstract val revisionName: Property<String>

    /** Submits [DeactivateRevisionAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(DeactivateRevisionAction::class.java) { params ->
            params.service.set(containerAppService)
            params.revisionName.set(revisionName)
        }
    }
}

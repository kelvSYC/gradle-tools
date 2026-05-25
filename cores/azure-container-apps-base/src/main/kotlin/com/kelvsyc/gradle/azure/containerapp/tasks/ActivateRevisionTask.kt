package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.ActivateRevisionAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Activates a specific revision of an Azure Container App.
 *
 * Delegates to [ActivateRevisionAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Activating a revision is not cacheable")
abstract class ActivateRevisionTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The scoped service identifying which app's revision to activate. */
    @get:Internal
    abstract val containerAppService: Property<ContainerAppBuildService>

    /** Full revision name (e.g. `my-app--abc123`). */
    @get:Input
    abstract val revisionName: Property<String>

    /** Submits [ActivateRevisionAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(ActivateRevisionAction::class.java) { params ->
            params.service.set(containerAppService)
            params.revisionName.set(revisionName)
        }
    }
}

package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.StopJobExecutionAction
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Cancels a running Container App Job execution.
 *
 * Delegates to [StopJobExecutionAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Stopping a job execution is not cacheable")
abstract class StopJobExecution @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The scoped service identifying which job owns the execution. */
    @get:Internal
    abstract val jobService: Property<ContainerAppJobBuildService>

    /** Full execution name to stop (e.g. `my-job--exec-abc123`). */
    @get:Input
    abstract val executionName: Property<String>

    /** Submits [StopJobExecutionAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(StopJobExecutionAction::class.java) { params ->
            params.service.set(jobService)
            params.executionName.set(executionName)
        }
    }
}

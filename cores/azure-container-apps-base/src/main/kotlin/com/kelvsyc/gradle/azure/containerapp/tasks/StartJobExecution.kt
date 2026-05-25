package com.kelvsyc.gradle.azure.containerapp.tasks

import com.kelvsyc.gradle.azure.containerapp.ContainerAppJobBuildService
import com.kelvsyc.gradle.azure.containerapp.actions.StartJobExecutionAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Starts a manual execution of a Container App Job and writes the execution name to
 * [executionNameFile].
 *
 * The execution name written to [executionNameFile] can be read by downstream tasks — for
 * example, a task that polls for completion using [GetJobExecutionValueSource].
 *
 * Delegates to [StartJobExecutionAction] via [WorkerExecutor.noIsolation].
 */
@DisableCachingByDefault(because = "Starting a job execution is not cacheable")
abstract class StartJobExecution @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The scoped service identifying which job to start. */
    @get:Internal
    abstract val jobService: Property<ContainerAppJobBuildService>

    /** File that will contain the resulting execution name after this task runs. */
    @get:OutputFile
    abstract val executionNameFile: RegularFileProperty

    /** Submits [StartJobExecutionAction]. */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(StartJobExecutionAction::class.java) { params ->
            params.service.set(jobService)
            params.executionNameFile.set(executionNameFile)
        }
    }
}

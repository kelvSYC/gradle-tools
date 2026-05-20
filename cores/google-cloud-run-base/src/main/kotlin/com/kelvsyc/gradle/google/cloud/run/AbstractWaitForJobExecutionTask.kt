package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Abstract task that reads an execution name from a file and waits for that
 * Cloud Run Job Execution to complete.
 *
 * This task is typically used as a downstream dependency to [AbstractRunJobTask],
 * accepting the execution name file written by that task.
 *
 * Internally delegates to [WaitForExecutionAction], which polls the execution
 * status until it reaches a terminal state.
 *
 * Prefer [WaitForJobExecutionTask] for direct task registration. Subclass this abstract form only when
 * you need to supply a custom `service` binding without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Waiting for a Cloud Run execution is not cacheable")
abstract class AbstractWaitForJobExecutionTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Cloud Run Executions client.
     */
    @get:Internal
    abstract val service: Property<CloudRunExecutionsClientBuildService>

    /**
     * The file containing the execution resource name to wait for.
     * Typically written by [AbstractRunJobTask.executionNameFile].
     */
    @get:InputFile
    abstract val executionNameFile: RegularFileProperty

    @TaskAction
    fun run() {
        val executionName = executionNameFile.get().asFile.readText().trim()
        workerExecutor.noIsolation().submit(WaitForExecutionAction::class.java) { params ->
            params.service.set(service)
            params.executionName.set(executionName)
        }
    }
}

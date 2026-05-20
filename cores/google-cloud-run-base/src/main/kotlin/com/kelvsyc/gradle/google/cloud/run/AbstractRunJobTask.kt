package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.EnvVar
import com.google.cloud.run.v2.RunJobRequest
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Abstract task that submits a Cloud Run Job Execution, optionally waiting for it to complete.
 *
 * Job submission is performed inline in the [@TaskAction] method rather than delegating
 * to a WorkAction. This is because the submission API returns an execution name (a String)
 * that must be threaded to [WaitForExecutionAction]. WorkAction parameters are strictly
 * input-only — there is no mechanism for a WorkAction to write a String value back to the
 * calling task. Using a companion-object or static state to share this value would be
 * an anti-pattern that breaks under parallel task execution.
 *
 * If [executionNameFile] is set, the execution name is written to disk and the task returns
 * immediately — waiting is deferred to a downstream [AbstractWaitForJobExecutionTask].
 * If [executionNameFile] is absent, the task waits inline via [WaitForExecutionAction].
 *
 * Prefer [RunJobTask] for direct task registration. Subclass this abstract form only when
 * you need to supply custom `service` or `executionsService` bindings without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Running a Cloud Run job is not cacheable")
abstract class AbstractRunJobTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Cloud Run Jobs client.
     */
    @get:Internal
    abstract val service: Property<CloudRunJobsClientBuildService>

    /**
     * The build service managing the Cloud Run Executions client.
     * Used to wait for the execution to complete.
     */
    @get:Internal
    abstract val executionsService: Property<CloudRunExecutionsClientBuildService>

    /**
     * The full resource name of the job to run: `projects/{p}/locations/{l}/jobs/{j}`.
     */
    @get:Input
    abstract val jobName: Property<String>

    /**
     * Per-run environment variable overrides.
     */
    @get:Input
    abstract val overrides: MapProperty<String, String>

    /**
     * If set, the execution name is written to this file and the task returns immediately
     * without waiting. A downstream [AbstractWaitForJobExecutionTask] can then wait for the
     * execution, allowing other build work to run in between. If absent, this task waits
     * inline via [WaitForExecutionAction].
     */
    @get:Optional
    @get:OutputFile
    abstract val executionNameFile: RegularFileProperty

    @TaskAction
    fun run() {
        val client = service.get().getClient()

        // Build RunJobRequest with overrides
        val runJobRequest = RunJobRequest.newBuilder()
            .setName(jobName.get())
            .also { builder ->
                if (overrides.isPresent && overrides.get().isNotEmpty()) {
                    val envOverrides = overrides.get().map { (k, v) ->
                        EnvVar.newBuilder().setName(k).setValue(v).build()
                    }
                    builder.setOverrides(
                        RunJobRequest.Overrides.newBuilder()
                            .addAllContainerOverrides(
                                listOf(
                                    RunJobRequest.Overrides.ContainerOverride.newBuilder()
                                        .addAllEnv(envOverrides)
                                        .build(),
                                ),
                            )
                            .build(),
                    )
                }
            }
            .build()

        // Submit the job and get the operation future
        val future = client.runJobAsync(runJobRequest)

        // Extract execution name without blocking on full completion
        var metadata = future.peekMetadata().get()
        var attempts = 0
        while (metadata == null && attempts < MAX_PEEK_ATTEMPTS) {
            Thread.sleep(PEEK_INTERVAL_MS)
            metadata = future.peekMetadata().get()
            attempts++
        }
        val executionName = checkNotNull(metadata) {
            "Could not retrieve execution name for job ${jobName.get()}"
        }.name

        if (executionNameFile.isPresent) {
            executionNameFile.get().asFile.writeText(executionName)
        } else {
            workerExecutor.noIsolation().submit(WaitForExecutionAction::class.java) { params ->
                params.service.set(executionsService)
                params.executionName.set(executionName)
            }
        }
    }

    companion object {
        private const val MAX_PEEK_ATTEMPTS = 10
        private const val PEEK_INTERVAL_MS = 500L
    }
}

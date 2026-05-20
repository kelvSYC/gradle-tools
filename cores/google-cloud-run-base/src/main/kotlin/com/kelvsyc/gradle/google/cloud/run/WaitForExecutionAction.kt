package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.ExecutionsClient
import org.gradle.api.GradleException
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal

/**
 * Work action that polls a Cloud Run Job Execution until it reaches a terminal state.
 *
 * This action repeatedly fetches the execution status, waiting for either successful completion
 * or failure. Polls every [pollIntervalMs] milliseconds (default 5000ms).
 */
abstract class WaitForExecutionAction : WorkAction<WaitForExecutionAction.Parameters> {

    override fun execute() {
        val client = parameters.service.get().getClient()
        val executionName = parameters.executionName.get()
        val pollInterval = parameters.pollIntervalMs.get()

        while (true) {
            val execution = client.getExecution(executionName)

            // Terminal state: completionTime is set
            if (execution.hasCompletionTime()) {
                // Check if execution failed
                if (execution.failedCount > 0) {
                    throw GradleException("Execution failed: ${execution.name}")
                }
                // Execution succeeded
                return
            }

            // Not yet complete, sleep and retry
            Thread.sleep(pollInterval)
        }
    }

    /**
     * Work parameters for [WaitForExecutionAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service providing the [ExecutionsClient].
         */
        @get:Internal
        val service: Property<CloudRunExecutionsClientBuildService>

        /**
         * Full execution resource name: `projects/{projectId}/locations/{location}/jobs/{jobId}/executions/{executionId}`.
         */
        val executionName: Property<String>

        /**
         * Polling interval in milliseconds. Defaults to 5000ms; override to 0 in tests.
         */
        @get:Internal
        val pollIntervalMs: Property<Long>
    }

    init {
        parameters.pollIntervalMs.convention(POLL_INTERVAL_MS)
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
    }
}

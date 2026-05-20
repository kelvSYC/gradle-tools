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
 * or failure. Polls every [pollIntervalMs] milliseconds (default 5000ms). Raises an exception if
 * the execution is not complete after [maxWaitTimeMs] (default 30 minutes).
 */
abstract class WaitForExecutionAction : WorkAction<WaitForExecutionAction.Parameters> {

    override fun execute() {
        val client = parameters.service.get().getClient()
        val executionName = parameters.executionName.get()
        val pollInterval = parameters.pollIntervalMs.get()
        val maxWaitMs = parameters.maxWaitTimeMs.get()

        val startTime = System.currentTimeMillis()

        while (true) {
            val execution = client.getExecution(executionName)

            // Terminal state: completionTime is set
            if (execution.hasCompletionTime()) {
                if (isExecutionFailed(execution)) {
                    throw GradleException(
                        "Execution ${execution.name} did not succeed " +
                        "(failedCount=${execution.failedCount}, cancelledCount=${execution.cancelledCount})",
                    )
                }
                // Execution succeeded
                return
            }

            // Check for timeout
            if (System.currentTimeMillis() - startTime >= maxWaitMs) {
                throw GradleException(
                    "Timed out waiting for execution $executionName " +
                    "(waited ${System.currentTimeMillis() - startTime}ms, max ${maxWaitMs}ms)",
                )
            }

            // Not yet complete, sleep and retry
            Thread.sleep(pollInterval)
        }
    }

    /**
     * Determines if an execution has failed or was cancelled.
     */
    private fun isExecutionFailed(execution: com.google.cloud.run.v2.Execution): Boolean =
        execution.failedCount > 0 || execution.cancelledCount > 0

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

        /**
         * Maximum time to wait for execution completion in milliseconds.
         * Defaults to 30 minutes. Raises [GradleException] if exceeded.
         */
        @get:Internal
        val maxWaitTimeMs: Property<Long>
    }

    init {
        parameters.pollIntervalMs.convention(POLL_INTERVAL_MS)
        parameters.maxWaitTimeMs.convention(DEFAULT_MAX_WAIT_MS)
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
        private const val DEFAULT_MAX_WAIT_MS = 30 * 60 * 1_000L  // 30 minutes
    }
}

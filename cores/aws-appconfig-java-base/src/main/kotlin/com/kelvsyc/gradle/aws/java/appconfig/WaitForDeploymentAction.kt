package com.kelvsyc.gradle.aws.java.appconfig

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.appconfig.model.DeploymentState
import software.amazon.awssdk.services.appconfig.model.GetDeploymentRequest

/**
 * [WorkAction] that polls an AppConfig deployment until it reaches a terminal state.
 *
 * Polls every [Parameters.pollIntervalMs] milliseconds (default 5 000 ms). Throws [GradleException]
 * if the deployment is rolled back or if [Parameters.maxWaitTimeMs] (default 30 min) is exceeded.
 * Terminal success state: [DeploymentState.COMPLETE].
 * Terminal failure state: [DeploymentState.ROLLED_BACK].
 */
abstract class WaitForDeploymentAction : WorkAction<WaitForDeploymentAction.Parameters> {

    /**
     * Parameters for [WaitForDeploymentAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the AppConfig client. */
        @get:Internal
        val service: Property<AppConfigClientBuildService>

        /** The application name or ID. */
        val applicationId: Property<String>

        /** The environment name or ID. */
        val environmentId: Property<String>

        /** The deployment number to poll. */
        val deploymentNumber: Property<Int>

        /** Polling interval in milliseconds. Override to `0` in tests. */
        @get:Internal
        val pollIntervalMs: Property<Long>

        /** Maximum time to wait in milliseconds before throwing [GradleException]. */
        @get:Internal
        val maxWaitTimeMs: Property<Long>
    }

    override fun execute() {
        val client = parameters.service.get().getClient()
        val pollInterval = parameters.pollIntervalMs.orElse(POLL_INTERVAL_MS).get()
        val maxWaitMs = parameters.maxWaitTimeMs.orElse(DEFAULT_MAX_WAIT_MS).get()
        val startTime = System.currentTimeMillis()

        while (true) {
            val request = GetDeploymentRequest.builder()
                .applicationId(parameters.applicationId.get())
                .environmentId(parameters.environmentId.get())
                .deploymentNumber(parameters.deploymentNumber.get())
                .build()
            val state = client.getDeployment(request).state()

            when (state) {
                DeploymentState.COMPLETE -> return
                DeploymentState.ROLLED_BACK -> throw GradleException(
                    "Deployment ${parameters.deploymentNumber.get()} was rolled back",
                )
                else -> {
                    if (System.currentTimeMillis() - startTime >= maxWaitMs) {
                        throw GradleException(
                            "Timed out waiting for deployment ${parameters.deploymentNumber.get()} " +
                            "(waited ${System.currentTimeMillis() - startTime}ms, max ${maxWaitMs}ms)",
                        )
                    }
                    Thread.sleep(pollInterval)
                }
            }
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5_000L
        private const val DEFAULT_MAX_WAIT_MS = 30L * 60L * 1_000L
    }
}

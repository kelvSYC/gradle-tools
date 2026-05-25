package com.kelvsyc.gradle.azure.containerapp

/**
 * Trigger type for a Container App Job.
 *
 * Maps to the `TriggerType` values in the Azure Container Apps ARM API.
 */
enum class JobTriggerType {
    /** Job must be started manually via the API or Gradle task. */
    MANUAL,

    /** Job runs on a cron schedule defined in [CreateContainerAppJobAction.Parameters.cronExpression]. */
    SCHEDULED,

    /** Job runs in response to KEDA scale events. */
    EVENT,
}

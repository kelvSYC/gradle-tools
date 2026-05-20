package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Specialization of [AbstractRunJobTask] that adds `@get:ServiceReference` to both
 * [service] and [executionsService] properties so Gradle automatically tracks the
 * build services as task dependencies.
 *
 * Register this task directly in most cases:
 *
 * ```kotlin
 * val cloudRunJobs = gradle.sharedServices.registerIfAbsent("cloudRunJobs", CloudRunJobsClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 * val cloudRunExecutions = gradle.sharedServices.registerIfAbsent("cloudRunExecutions", CloudRunExecutionsClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 *
 * tasks.register<RunJobTask>("runJob") {
 *     service.set(cloudRunJobs)
 *     executionsService.set(cloudRunExecutions)
 *     jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
 *     overrides.put("ENV_VAR", "override-value")
 *     executionNameFile.set(file("build/execution-name.txt"))
 * }
 * ```
 */
abstract class RunJobTask @Inject constructor(
    workerExecutor: WorkerExecutor,
) : AbstractRunJobTask(workerExecutor) {

    @get:ServiceReference
    abstract override val service: Property<CloudRunJobsClientBuildService>

    @get:ServiceReference
    abstract override val executionsService: Property<CloudRunExecutionsClientBuildService>
}

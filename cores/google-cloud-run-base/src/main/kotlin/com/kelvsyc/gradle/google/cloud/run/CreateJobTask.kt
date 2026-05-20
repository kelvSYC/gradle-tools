package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Specialization of [AbstractCreateJobTask] that adds `@get:ServiceReference` to the
 * [service] property so Gradle automatically tracks the build service as a task dependency.
 *
 * Register this task directly in most cases:
 *
 * ```kotlin
 * val cloudRunJobs = gradle.sharedServices.registerIfAbsent("cloudRunJobs", CloudRunJobsClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 *
 * tasks.register<CreateJobTask>("createJob") {
 *     service.set(cloudRunJobs)
 *     jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
 *     imageUri.set("gcr.io/my-project/image:tag")
 *     envVars.put("ENV_VAR", "value")
 * }
 * ```
 */
abstract class CreateJobTask @Inject constructor(
    workerExecutor: WorkerExecutor,
) : AbstractCreateJobTask(workerExecutor) {

    @get:ServiceReference
    abstract override val service: Property<CloudRunJobsClientBuildService>
}

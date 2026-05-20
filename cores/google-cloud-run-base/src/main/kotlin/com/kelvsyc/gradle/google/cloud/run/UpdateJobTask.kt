package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Specialization of [AbstractUpdateJobTask] that adds `@get:ServiceReference` to the
 * [service] property so Gradle automatically tracks the build service as a task dependency.
 *
 * Register this task directly in most cases:
 *
 * ```kotlin
 * val cloudRunJobs = gradle.sharedServices.registerIfAbsent("cloudRunJobs", CloudRunJobsClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 *
 * tasks.register<UpdateJobTask>("updateJob") {
 *     service.set(cloudRunJobs)
 *     jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
 *     imageUri.set("gcr.io/my-project/image:tag")
 *     envVars.put("ENV_VAR", "value")
 * }
 * ```
 */
abstract class UpdateJobTask @Inject constructor(
    workerExecutor: WorkerExecutor,
) : AbstractUpdateJobTask(workerExecutor) {

    @get:ServiceReference
    abstract override val service: Property<CloudRunJobsClientBuildService>
}

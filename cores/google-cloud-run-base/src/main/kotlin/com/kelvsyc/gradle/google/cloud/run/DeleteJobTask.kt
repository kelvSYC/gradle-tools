package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference

/**
 * Specialization of [AbstractDeleteJobTask] that adds `@get:ServiceReference` to the
 * [service] property so Gradle automatically tracks the build service as a task dependency.
 *
 * Register this task directly in most cases:
 *
 * ```kotlin
 * val cloudRunJobs = gradle.sharedServices.registerIfAbsent("cloudRunJobs", CloudRunJobsClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 *
 * tasks.register<DeleteJobTask>("deleteJob") {
 *     service.set(cloudRunJobs)
 *     jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
 * }
 * ```
 */
abstract class DeleteJobTask : AbstractDeleteJobTask() {

    @get:ServiceReference
    abstract override val service: Property<CloudRunJobsClientBuildService>
}

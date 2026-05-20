package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Specialization of [AbstractUpsertServiceTask] that adds `@get:ServiceReference` to the
 * [service] property so Gradle automatically tracks the build service as a task dependency.
 *
 * Register this task directly in most cases:
 *
 * ```kotlin
 * val cloudRun = gradle.sharedServices.registerIfAbsent("cloudRun", CloudRunServicesClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 *
 * tasks.register<UpsertServiceTask>("deployService") {
 *     service.set(cloudRun)
 *     serviceName.set("projects/my-project/locations/us-central1/services/my-service")
 *     imageUri.set("gcr.io/my-project/image:tag")
 *     envVars.put("ENV_VAR", "value")
 * }
 * ```
 */
abstract class UpsertServiceTask @Inject constructor(
    workerExecutor: WorkerExecutor,
) : AbstractUpsertServiceTask(workerExecutor) {

    @get:ServiceReference
    abstract override val service: Property<CloudRunServicesClientBuildService>
}

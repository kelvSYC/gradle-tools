package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference

/**
 * Specialization of [AbstractDeleteServiceTask] that adds `@get:ServiceReference` to the
 * [service] property so Gradle automatically tracks the build service as a task dependency.
 *
 * Register this task directly in most cases:
 *
 * ```kotlin
 * val cloudRun = gradle.sharedServices.registerIfAbsent("cloudRun", CloudRunServicesClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 *
 * tasks.register<DeleteServiceTask>("deleteService") {
 *     service.set(cloudRun)
 *     serviceName.set("projects/my-project/locations/us-central1/services/my-service")
 * }
 * ```
 */
abstract class DeleteServiceTask : AbstractDeleteServiceTask() {

    @get:ServiceReference
    abstract override val service: Property<CloudRunServicesClientBuildService>
}

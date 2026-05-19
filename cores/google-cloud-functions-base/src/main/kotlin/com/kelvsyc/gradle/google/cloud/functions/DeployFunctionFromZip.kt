package com.kelvsyc.gradle.google.cloud.functions

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Specialization of [AbstractDeployFunctionFromZip] that adds `@get:ServiceReference` to the
 * [service] property so Gradle automatically tracks the build service as a task dependency.
 *
 * Register this task directly in most cases:
 *
 * ```kotlin
 * val functions = gradle.sharedServices.registerIfAbsent("functions", FunctionServiceClientBuildService::class) {
 *     parameters { applicationDefault() }
 * }
 *
 * tasks.register<DeployFunctionFromZip>("deploy") {
 *     service.set(functions)
 *     functionName.set("projects/my-project/locations/us-central1/functions/my-function")
 *     zipFile.set(layout.buildDirectory.file("dist/function.zip"))
 * }
 * ```
 */
@DisableCachingByDefault(because = "Deploying to an external service is not cacheable")
abstract class DeployFunctionFromZip @Inject constructor(
    workerExecutor: WorkerExecutor,
) : AbstractDeployFunctionFromZip(workerExecutor) {

    @get:ServiceReference
    abstract override val service: Property<FunctionServiceClientBuildService>
}

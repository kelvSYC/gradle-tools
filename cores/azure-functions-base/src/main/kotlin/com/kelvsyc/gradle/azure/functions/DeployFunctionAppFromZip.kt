package com.kelvsyc.gradle.azure.functions

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Task that deploys a local zip file to an Azure Function App via the Kudu SCM zip-deploy API.
 *
 * Adds `@ServiceReference` to [appService] so Gradle automatically tracks the build service as
 * a task dependency. Use this class for direct task registration:
 *
 * ```kotlin
 * tasks.register<DeployFunctionAppFromZip>("deployFunction") {
 *     appService.set(functions)
 *     appName.set("my-function-app")
 *     zipFile.set(layout.buildDirectory.file("dist/app.zip"))
 * }
 * ```
 *
 * Subclass [AbstractDeployFunctionAppFromZip] directly when you need to bind [appService]
 * without `@ServiceReference` tracking (e.g. in a composite build with custom wiring).
 */
@DisableCachingByDefault(because = "Deploying to an external service is not cacheable")
abstract class DeployFunctionAppFromZip @Inject constructor(
    workerExecutor: WorkerExecutor,
) : AbstractDeployFunctionAppFromZip(workerExecutor) {

    @get:ServiceReference
    abstract override val appService: Property<FunctionAppClientBuildService>
}

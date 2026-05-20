package com.kelvsyc.gradle.azure.functions

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Base task that deploys a local zip file to an Azure Function App via the Kudu SCM zip-deploy API.
 *
 * Internally delegates to [ZipDeployFunctionAppAction]. The [zipFile] is declared `@InputFile` so
 * Gradle tracks it for up-to-date checking and can wire it to an upstream archive task.
 *
 * Prefer [DeployFunctionAppFromZip] for direct registration. Subclass this form only when you need
 * to supply a custom [appService] binding without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Deploying to an external service is not cacheable")
abstract class AbstractDeployFunctionAppFromZip @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The ARM manager service used to retrieve publishing credentials and locate the function app.
     */
    @get:Internal
    abstract val appService: Property<FunctionAppClientBuildService>

    /**
     * The name of the function app to deploy to.
     */
    @get:Input
    abstract val appName: Property<String>

    /**
     * The local zip file to deploy. Re-deployment is triggered when this file changes.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val zipFile: RegularFileProperty

    /**
     * Submits a [ZipDeployFunctionAppAction] to deploy the zip to the configured function app.
     */
    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(ZipDeployFunctionAppAction::class.java) { params ->
            params.appService.set(appService)
            params.appName.set(appName)
            params.zipFile.set(zipFile)
        }
    }
}

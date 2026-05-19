package com.kelvsyc.gradle.google.cloud.functions

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
 * Task that uploads a local zip file to Cloud Functions Gen 2 as a new deployment.
 *
 * Internally delegates to [UploadAndUpdateFunctionAction], which calls `generateUploadUrl`, HTTP
 * PUT the zip, then waits for `updateFunction` to complete. The [zipFile] is declared as an
 * `@InputFile` so Gradle tracks it for up-to-date checking and can wire it to an upstream task
 * that produces the zip.
 *
 * Prefer [DeployFunctionFromZip] for direct task registration. Subclass this abstract form only
 * when you need to supply a custom `service` binding without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Deploying to an external service is not cacheable")
abstract class AbstractDeployFunctionFromZip @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /** The build service managing the Cloud Functions client. */
    @get:Internal
    abstract val service: Property<FunctionServiceClientBuildService>

    /**
     * The full resource name of the function to update, e.g.
     * `projects/my-project/locations/us-central1/functions/my-function`.
     */
    @get:Input
    abstract val functionName: Property<String>

    /** The local zip file to upload and deploy. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val zipFile: RegularFileProperty

    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(UploadAndUpdateFunctionAction::class.java) { params ->
            params.service.set(service)
            params.functionName.set(functionName)
            params.zipFile.set(zipFile)
        }
    }
}

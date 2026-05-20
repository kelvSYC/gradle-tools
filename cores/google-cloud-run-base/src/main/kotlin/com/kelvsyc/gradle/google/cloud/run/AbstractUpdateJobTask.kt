package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Abstract task that updates an existing Cloud Run Job definition.
 *
 * Internally delegates to [UpdateJobAction], which fetches the existing job, then updates
 * its container image and environment variables.
 *
 * Prefer [UpdateJobTask] for direct task registration. Subclass this abstract form only when
 * you need to supply a custom `service` binding without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Updating a Cloud Run job is not cacheable")
abstract class AbstractUpdateJobTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Cloud Run Jobs client.
     */
    @get:Internal
    abstract val service: Property<CloudRunJobsClientBuildService>

    /**
     * The full resource name of the job to update,
     * e.g. `projects/my-project/locations/us-central1/jobs/my-job`.
     */
    @get:Input
    abstract val jobName: Property<String>

    /**
     * The container image URI to deploy, e.g. `gcr.io/my-project/image:tag`.
     */
    @get:Input
    abstract val imageUri: Property<String>

    /**
     * Environment variables to set on the container.
     */
    @get:Input
    abstract val envVars: MapProperty<String, String>

    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(UpdateJobAction::class.java) { params ->
            params.service.set(service)
            params.jobName.set(jobName)
            params.imageUri.set(imageUri)
            params.envVars.set(envVars)
        }
    }
}

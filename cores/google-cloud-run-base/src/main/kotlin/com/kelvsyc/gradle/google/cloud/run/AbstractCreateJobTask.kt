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
 * Abstract task that creates a Cloud Run Job definition.
 *
 * Internally delegates to [CreateJobAction], which parses the job ID from the full resource name,
 * then creates a new job with the specified container image and environment variables.
 * The container is set on the job's execution template.
 *
 * Prefer [CreateJobTask] for direct task registration. Subclass this abstract form only when
 * you need to supply a custom `service` binding without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Creating a Cloud Run job is not cacheable")
abstract class AbstractCreateJobTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Cloud Run Jobs client.
     */
    @get:Internal
    abstract val service: Property<CloudRunJobsClientBuildService>

    /**
     * The full resource name of the job to create,
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
        workerExecutor.noIsolation().submit(CreateJobAction::class.java) { params ->
            params.service.set(service)
            params.jobName.set(jobName)
            params.imageUri.set(imageUri)
            params.envVars.set(envVars)
        }
    }
}

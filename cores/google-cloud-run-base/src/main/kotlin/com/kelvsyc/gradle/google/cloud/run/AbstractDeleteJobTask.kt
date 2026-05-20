package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Abstract task that deletes a Cloud Run Job definition.
 *
 * Internally delegates to [DeleteJobAction], which deletes the job and blocks until
 * the long-running operation completes.
 *
 * Prefer [DeleteJobTask] for direct task registration. Subclass this abstract form only when
 * you need to supply a custom `service` binding without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Deleting a Cloud Run job is not cacheable")
abstract class AbstractDeleteJobTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Cloud Run Jobs client.
     */
    @get:Internal
    abstract val service: Property<CloudRunJobsClientBuildService>

    /**
     * The full resource name of the job to delete,
     * e.g. `projects/my-project/locations/us-central1/jobs/my-job`.
     */
    @get:Input
    abstract val jobName: Property<String>

    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(DeleteJobAction::class.java) { params ->
            params.service.set(service)
            params.jobName.set(jobName)
        }
    }
}

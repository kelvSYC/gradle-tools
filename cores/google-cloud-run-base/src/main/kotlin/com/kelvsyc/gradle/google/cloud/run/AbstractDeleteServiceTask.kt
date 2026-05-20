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
 * Abstract task that deletes a Cloud Run Service.
 *
 * Internally delegates to [DeleteServiceAction], which deletes the service and blocks until
 * the long-running operation completes.
 *
 * Prefer [DeleteServiceTask] for direct task registration. Subclass this abstract form only when
 * you need to supply a custom `service` binding without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Deleting a Cloud Run service is not cacheable")
abstract class AbstractDeleteServiceTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Cloud Run Services client.
     */
    @get:Internal
    abstract val service: Property<CloudRunServicesClientBuildService>

    /**
     * The full resource name of the service to delete,
     * e.g. `projects/my-project/locations/us-central1/services/my-service`.
     */
    @get:Input
    abstract val serviceName: Property<String>

    @TaskAction
    fun run() {
        workerExecutor.noIsolation().submit(DeleteServiceAction::class.java) { params ->
            params.service.set(service)
            params.serviceName.set(serviceName)
        }
    }
}

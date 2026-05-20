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
 * Abstract task that creates or updates a Cloud Run Service (upsert semantics).
 *
 * Internally delegates to [UpsertServiceAction], which fetches the full resource name to extract
 * the service ID and parent location, then attempts to update an existing service. If the service
 * does not exist, it creates a new one. The container image and environment variables are set
 * on the service's revision template.
 *
 * Prefer [UpsertServiceTask] for direct task registration. Subclass this abstract form only when
 * you need to supply a custom `service` binding without `@ServiceReference` tracking.
 */
@DisableCachingByDefault(because = "Deploying to Cloud Run is not cacheable")
abstract class AbstractUpsertServiceTask @Inject constructor(
    private val workerExecutor: WorkerExecutor,
) : DefaultTask() {

    /**
     * The build service managing the Cloud Run Services client.
     */
    @get:Internal
    abstract val service: Property<CloudRunServicesClientBuildService>

    /**
     * The full resource name of the service to create or update,
     * e.g. `projects/my-project/locations/us-central1/services/my-service`.
     */
    @get:Input
    abstract val serviceName: Property<String>

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
        workerExecutor.noIsolation().submit(UpsertServiceAction::class.java) { params ->
            params.service.set(service)
            params.serviceName.set(serviceName)
            params.imageUri.set(imageUri)
            params.envVars.set(envVars)
        }
    }
}

package com.kelvsyc.gradle.google.cloud.run

import com.google.api.gax.rpc.NotFoundException
import com.google.cloud.run.v2.Container
import com.google.cloud.run.v2.EnvVar
import com.google.cloud.run.v2.RevisionTemplate
import com.google.cloud.run.v2.Service
import com.google.protobuf.FieldMask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that creates or updates a Cloud Run Service (upsert semantics).
 *
 * The action fetches the full resource name to extract the service ID and parent location,
 * then attempts to update an existing service. If the service does not exist, it creates a new one.
 * The container image and environment variables are set on the service's revision template.
 *
 * This action blocks until the long-running operation completes.
 */
abstract class UpsertServiceAction : WorkAction<UpsertServiceAction.Parameters> {

    /**
     * Parameters for [UpsertServiceAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Cloud Run Services client.
         */
        @get:Internal
        val service: Property<CloudRunServicesClientBuildService>

        /**
         * The full resource name of the service to create or update,
         * e.g. `projects/my-project/locations/us-central1/services/my-service`.
         */
        val serviceName: Property<String>

        /**
         * The container image URI to deploy, e.g. `gcr.io/my-project/image:tag`.
         */
        val imageUri: Property<String>

        /**
         * Environment variables to set on the container.
         */
        val envVars: MapProperty<String, String>
    }

    override fun execute() {
        val client = parameters.service.get().getClient()
        val serviceName = parameters.serviceName.get()
        val imageUri = parameters.imageUri.get()
        val envVarsMap = parameters.envVars.get()

        // Extract service ID and parent from the full resource name
        val serviceId = serviceName.substringAfterLast("/")
        val parent = serviceName.substringBeforeLast("/services/")

        // Build the container with image and env vars
        val containerBuilder = Container.newBuilder()
            .setImage(imageUri)
        for ((key, value) in envVarsMap) {
            containerBuilder.addEnv(
                EnvVar.newBuilder()
                    .setName(key)
                    .setValue(value)
                    .build()
            )
        }

        try {
            // Try to fetch the existing service
            val existingService = client.getService(serviceName)

            // Update the existing service
            val updated = existingService.toBuilder()
                .setTemplate(
                    existingService.template.toBuilder()
                        .clearContainers()
                        .addContainers(containerBuilder.build())
                        .build()
                )
                .build()

            client.updateServiceAsync(
                updated,
                FieldMask.newBuilder()
                    .addPaths("template.containers")
                    .build()
            ).get()
        } catch (@Suppress("SwallowedException") e: NotFoundException) {
            // Service does not exist, create it
            val newService = Service.newBuilder()
                .setTemplate(
                    RevisionTemplate.newBuilder()
                        .addContainers(containerBuilder.build())
                        .build()
                )
                .build()

            client.createServiceAsync(parent, newService, serviceId).get()
        }
    }
}

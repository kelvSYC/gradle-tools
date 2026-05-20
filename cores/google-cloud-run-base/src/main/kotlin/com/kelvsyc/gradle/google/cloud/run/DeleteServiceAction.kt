package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that deletes a Cloud Run Service.
 *
 * This action blocks until the long-running operation completes.
 */
abstract class DeleteServiceAction : WorkAction<DeleteServiceAction.Parameters> {

    /**
     * Parameters for [DeleteServiceAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The build service managing the Cloud Run Services client.
         */
        @get:Internal
        val service: Property<CloudRunServicesClientBuildService>

        /**
         * The full resource name of the service to delete,
         * e.g. `projects/my-project/locations/us-central1/services/my-service`.
         */
        val serviceName: Property<String>
    }

    override fun execute() {
        val client = parameters.service.get().getClient()
        client.deleteServiceAsync(parameters.serviceName.get()).get()
    }
}

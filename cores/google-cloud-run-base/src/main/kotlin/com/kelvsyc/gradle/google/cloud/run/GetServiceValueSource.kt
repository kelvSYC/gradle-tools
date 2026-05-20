package com.kelvsyc.gradle.google.cloud.run

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing the HTTPS endpoint URL of a Cloud Run service.
 *
 * Returns `null` if the service does not exist or has no deployed URI.
 *
 * The [Parameters.serviceName] must be the full resource name in the form
 * `projects/{project}/locations/{location}/services/{service}`.
 */
abstract class GetServiceValueSource : ValueSource<String, GetServiceValueSource.Parameters> {

    /**
     * Parameters for [GetServiceValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the Cloud Run Services client.
         */
        @get:Internal
        val service: Property<CloudRunServicesClientBuildService>

        /**
         * The full resource name of the service, e.g.
         * `projects/my-project/locations/us-central1/services/my-service`.
         */
        val serviceName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            val service = parameters.service.get().getClient().getService(parameters.serviceName.get())
            service.uri.takeIf { it.isNotBlank() }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            if (e.javaClass.name == "com.google.api.gax.rpc.NotFoundException") null else throw e
        }
    }
}

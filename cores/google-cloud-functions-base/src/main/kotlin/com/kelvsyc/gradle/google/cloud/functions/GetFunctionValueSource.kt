package com.kelvsyc.gradle.google.cloud.functions

import com.google.cloud.functions.v2.GetFunctionRequest
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing the HTTPS trigger URI of a Cloud Functions Gen 2 function.
 *
 * Returns `null` if the function has no deployed URI (e.g. still provisioning).
 *
 * The [Parameters.functionName] must be the full resource name in the form
 * `projects/{project}/locations/{location}/functions/{function}`.
 */
abstract class GetFunctionValueSource : ValueSource<String, GetFunctionValueSource.Parameters> {

    /**
     * Parameters for [GetFunctionValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Cloud Functions client. */
        @get:Internal
        val service: Property<FunctionServiceClientBuildService>

        /**
         * The full resource name of the function, e.g.
         * `projects/my-project/locations/us-central1/functions/my-function`.
         */
        val functionName: Property<String>
    }

    override fun obtain(): String? {
        val request = GetFunctionRequest.newBuilder()
            .setName(parameters.functionName.get())
            .build()
        val function = parameters.service.get().getClient().getFunction(request)
        return function.serviceConfig.uri.takeIf { it.isNotBlank() }
    }
}

package com.kelvsyc.gradle.artifactory

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.ArtifactoryRequest
import org.jfrog.artifactory.client.ArtifactoryStreamingResponse

/**
 * Base class for [ValueSource] implementations that provide a value from performing a streaming REST API call to
 * Artifactory.
 *
 * Subclasses should implement the [doObtain] function, transforming an [ArtifactoryStreamingResponse] object to an
 * object of the desired type.
 */
abstract class AbstractStreamingRequestValueSource<T : Any, P : AbstractStreamingRequestValueSource.Parameters>
    : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractStreamingRequestValueSource]. This contains the data needed to retrieve
     * an object from Artifactory.
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractStreamingRequestValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val request: Property<ArtifactoryRequest>
    }

    private val client: Provider<Artifactory> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    /**
     * Transforms the response returned from Artifactory.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from Artifactory, or `null` if the data cannot be transformed into the needed data.
     */
    abstract fun doObtain(response: ArtifactoryStreamingResponse): T?

    override fun obtain(): T? {
        val response = client.get().streamingRestCall(parameters.request.get())
        return doObtain(response)
    }
}

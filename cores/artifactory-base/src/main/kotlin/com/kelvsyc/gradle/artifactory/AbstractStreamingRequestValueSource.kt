package com.kelvsyc.gradle.artifactory

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import org.jfrog.artifactory.client.ArtifactoryRequest
import org.jfrog.artifactory.client.ArtifactoryStreamingResponse

/**
 * Base class for [ValueSource] implementations that provide a value from performing a streaming REST API call to
 * Artifactory.
 *
 * Subclasses should implement the [doObtain] function, transforming an [ArtifactoryStreamingResponse] object to an
 * object of the desired type.
 *
 * **Configuration cache and sensitive responses:** Gradle serializes the result of every [ValueSource.obtain] call
 * to the configuration cache in plaintext when the cache is written. Whatever [doObtain] returns — including any
 * sensitive content the API response may contain — will be stored in `.gradle/configuration-cache/` and is
 * readable by any process with access to the build directory. This applies regardless of how the resulting
 * [org.gradle.api.provider.Provider] is stored: wiring it into a task `@Input` property, a `@get:Internal`
 * property, or a private `val` all cause `obtain()` to run at configuration time and the result to be cached.
 *
 * If the API response may contain sensitive data, call the [ArtifactoryClientBuildService] client directly inside
 * a [org.gradle.workers.WorkAction.execute] body instead, where the result is never written to the cache.
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
        @get:Internal
        val service: Property<ArtifactoryClientBuildService>

        @get:Internal
        val request: Property<ArtifactoryRequest>
    }

    /**
     * Transforms the response returned from Artifactory.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from Artifactory, or `null` if the data cannot be transformed into the needed data.
     */
    abstract fun doObtain(response: ArtifactoryStreamingResponse): T?

    override fun obtain(): T? {
        val response = parameters.service.get().getClient()
            .streamingRestCall(parameters.request.get())
        return doObtain(response)
    }
}

package com.kelvsyc.gradle.artifactory

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import java.io.InputStream

/**
 * Base class for [ValueSource] implementations that provide a value from reading an artifact downloaded from
 * Artifactory.
 *
 * Subclasses should implement the [doObtain] function, transforming an [InputStream] object to an object of the
 * desired type.
 */
abstract class AbstractArtifactValueSource<T : Any, P : AbstractArtifactValueSource.Parameters> : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractArtifactValueSource]. This contains the data needed to retrieve an object from
     * Artifactory.
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractArtifactValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
        @get:Internal
        val service: Property<ArtifactoryClientBuildService>

        val repository: Property<String>
        val path: Property<String>
    }

    /**
     * Transforms the data retrieved from Artifactory.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from Artifactory, or `null` if the data cannot be transformed into the needed data.
     */
    abstract fun doObtain(input: InputStream): T?

    override fun obtain(): T? {
        val artifact = parameters.service.get().getClient()
            .repository(parameters.repository.get())
            .download(parameters.path.get())

        return doObtain(artifact.doDownload())
    }
}

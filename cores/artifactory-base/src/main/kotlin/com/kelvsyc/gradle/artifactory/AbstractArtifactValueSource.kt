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
 *
 * **Configuration cache and sensitive artifacts:** Gradle serializes the result of every [ValueSource.obtain] call
 * to the configuration cache in plaintext when the cache is written. Whatever [doObtain] returns — including any
 * sensitive content the artifact may contain (credentials, private keys, tokens) — will be stored in
 * `.gradle/configuration-cache/` and is readable by any process with access to the build directory. This applies
 * regardless of how the resulting [org.gradle.api.provider.Provider] is stored: wiring it into a task `@Input`
 * property, a `@get:Internal` property, or a private `val` all cause `obtain()` to run at configuration time and
 * the result to be cached.
 *
 * If the fetched artifact may contain sensitive data, call the [ArtifactoryClientBuildService] client directly
 * inside a [org.gradle.workers.WorkAction.execute] body instead, where the result is never written to the cache.
 * Non-sensitive artifacts (version manifests, metadata, changelogs) are safe to use at configuration time.
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

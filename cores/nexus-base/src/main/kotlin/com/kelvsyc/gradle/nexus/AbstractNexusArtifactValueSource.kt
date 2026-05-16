package com.kelvsyc.gradle.nexus

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import java.io.InputStream

/**
 * Base class for [ValueSource] implementations that provide a value by downloading an artifact
 * from a Nexus raw repository at configuration time.
 *
 * Subclasses implement [doObtain] to transform the response [InputStream] into the desired type.
 *
 * **Configuration cache and sensitive artifacts:** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext when the cache is written.
 * Whatever [doObtain] returns — including any sensitive content the artifact may contain
 * (credentials, private keys, tokens) — will be stored in `.gradle/configuration-cache/` and is
 * readable by any process with access to the build directory. This applies regardless of how the
 * resulting [org.gradle.api.provider.Provider] is stored: wiring it into a task `@Input` property,
 * a `@get:Internal` property, or a private `val` all cause `obtain()` to run at configuration time
 * and the result to be cached.
 *
 * If the fetched artifact may contain sensitive data, call the [NexusClientBuildService] client
 * directly inside a [org.gradle.workers.WorkAction.execute] body instead, where the result is
 * never written to the cache. Non-sensitive artifacts (version manifests, metadata, changelogs)
 * are safe to use at configuration time.
 */
abstract class AbstractNexusArtifactValueSource<T : Any> :
    ValueSource<T, AbstractNexusArtifactValueSource.Parameters> {

    /**
     * Parameters for [AbstractNexusArtifactValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the Nexus client.
         */
        @get:Internal
        val service: Property<NexusClientBuildService>

        /**
         * The name of the Nexus repository containing the artifact.
         */
        val repository: Property<String>

        /**
         * The path to the artifact within the repository (e.g. `com/example/1.0/artifact-1.0.jar`).
         */
        val path: Property<String>
    }

    /**
     * Transforms the artifact bytes downloaded from Nexus.
     *
     * Called once per [obtain] invocation with the response [InputStream]. The stream is
     * closed after this method returns.
     *
     * @return The transformed value, or `null` if the data cannot be transformed.
     */
    abstract fun doObtain(input: InputStream): T?

    override fun obtain(): T? {
        val response = parameters.service.get().getClient()
            .downloadAsset(parameters.repository.get(), parameters.path.get())
            .execute()
        return response.body()?.byteStream()?.use { doObtain(it) }
    }
}

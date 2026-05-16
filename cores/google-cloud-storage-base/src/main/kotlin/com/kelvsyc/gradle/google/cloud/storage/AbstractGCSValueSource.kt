package com.kelvsyc.gradle.google.cloud.storage

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource] implementations that provide a value by reading a value from GCS.
 *
 * Subclasses should implement the [doObtain] function, transforming a [ByteArray] to an object of the desired type.
 * This class should only be used on blobs for which the entire blob can be kept in memory.
 *
 * **Configuration cache and sensitive blobs:** Gradle serializes the result of every [ValueSource.obtain] call
 * to the configuration cache in plaintext when the cache is written. Whatever [doObtain] returns — including any
 * sensitive content the GCS blob may contain (credentials, private keys, tokens) — will be stored in
 * `.gradle/configuration-cache/` and is readable by any process with access to the build directory. This applies
 * regardless of how the resulting [org.gradle.api.provider.Provider] is stored: wiring it into a task `@Input`
 * property, a `@get:Internal` property, or a private `val` all cause `obtain()` to run at configuration time and
 * the result to be cached.
 *
 * If the fetched blob may contain sensitive data, call the [StorageClientBuildService] client directly inside a
 * [org.gradle.workers.WorkAction.execute] body instead, where the result is never written to the cache.
 * Non-sensitive blobs (version manifests, metadata, changelogs) are safe to use at configuration time.
 */
abstract class AbstractGCSValueSource<T : Any, P : AbstractGCSValueSource.Parameters> : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractGCSValueSource]. This contains the data needed to retrieve a blob from
     * GCS.
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractGCSValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
        @get:Internal
        val service: Property<StorageClientBuildService>

        val bucket: Property<String>
        val blobName: Property<String>
    }

    /**
     * Transforms the data retrieved from GCS.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from GCS, or `null` if the data cannot be transformed into the needed data.
     */
    abstract fun doObtain(content: ByteArray): T?

    override fun obtain(): T? {
        val content = parameters.service.get().getClient()
            .readAllBytes(parameters.bucket.get(), parameters.blobName.get())
        return doObtain(content)
    }
}

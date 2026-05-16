package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.util.BinaryData
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource] implementations that provide a value by reading a blob from Azure Blob Storage.
 *
 * Subclasses should implement the [doObtain] function, transforming a [BinaryData] object to an object of the
 * desired type. This class should only be used on blobs for which the entire blob can be kept in memory.
 *
 * **Configuration cache and sensitive blobs:** Gradle serializes the result of every [ValueSource.obtain] call
 * to the configuration cache in plaintext when the cache is written. Whatever [doObtain] returns — including any
 * sensitive content the blob may contain (credentials, private keys, tokens) — will be stored in
 * `.gradle/configuration-cache/` and is readable by any process with access to the build directory. This applies
 * regardless of how the resulting [org.gradle.api.provider.Provider] is stored: wiring it into a task `@Input`
 * property, a `@get:Internal` property, or a private `val` all cause `obtain()` to run at configuration time and
 * the result to be cached.
 *
 * If the fetched blob may contain sensitive data, call the [BlobServiceClientBuildService] client directly inside
 * a [org.gradle.workers.WorkAction.execute] body instead, where the result is never written to the cache.
 * Non-sensitive blobs (version manifests, metadata, changelogs) are safe to use at configuration time.
 */
abstract class AbstractBlobStorageValueSource<T : Any, P : AbstractBlobStorageValueSource.Parameters> :
    ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractBlobStorageValueSource].
     *
     * Extend this interface if there is a need to supply additional parameters to the subclass.
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the account-scoped Blob Service client. */
        @get:Internal
        val service: Property<BlobServiceClientBuildService>

        /** The name of the blob container. */
        val containerName: Property<String>

        /** The name of the blob within the container. */
        val blobName: Property<String>
    }

    /**
     * Transforms the data retrieved from Azure Blob Storage.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from Azure Blob Storage, or `null` if the data cannot be transformed.
     */
    abstract fun doObtain(content: BinaryData): T?

    override fun obtain(): T? {
        val blobClient = parameters.service.get().getClient()
            .getBlobContainerClient(parameters.containerName.get())
            .getBlobClient(parameters.blobName.get())
        return doObtain(blobClient.downloadContent())
    }
}

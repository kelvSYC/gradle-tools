package com.kelvsyc.gradle.google.cloud.storage

import com.google.cloud.storage.Storage
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * Base class for [ValueSource] implementations that provide a value by reading a value from GCS.
 *
 * Subclasses should implement the [doObtain] function, transforming a [ByteArray] to an object of the desired type.
 * This class should only be used on blobs for which the entire blob can be kept in memory.
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
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val bucket: Property<String>
        val blobName: Property<String>
    }

    private val client: Provider<Storage> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    /**
     * Transforms the data retrieved from GCS.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from GCS, or `null` if the data cannot be transformed into the needed data.
     */
    abstract fun doObtain(content: ByteArray): T?

    override fun obtain(): T? {
        val content = client.get().readAllBytes(parameters.bucket.get(), parameters.blobName.get())
        return doObtain(content)
    }
}

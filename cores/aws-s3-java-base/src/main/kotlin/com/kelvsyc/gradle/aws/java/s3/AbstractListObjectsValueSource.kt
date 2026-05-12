package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.S3Object
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource] implementations that produce a value by listing objects in an S3 bucket.
 *
 * Pagination is handled internally; subclasses receive the full list of [S3Object] summaries across all pages
 * via [doObtain] and transform it to the desired type.
 */
abstract class AbstractListObjectsValueSource<T : Any, P : AbstractListObjectsValueSource.Parameters>
    : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractListObjectsValueSource].
     *
     * Extend this interface if there is a need to supply additional parameters to the subclass.
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The shared [ClientsBaseService] holding the registered S3 client.
         */
        @get:Internal
        val service: Property<ClientsBaseService>

        /**
         * Registered name of an [S3ClientInfo].
         */
        val clientName: Property<String>

        /**
         * S3 bucket name.
         */
        val bucket: Property<String>

        /**
         * Optional key prefix used to filter the listing.
         */
        val prefix: Property<String>
    }

    private val client: Provider<S3Client> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    /**
     * Transforms the listed objects into the target type.
     *
     * @param objects All [S3Object] summaries returned across paginated responses.
     * @return The transformed value, or `null` if the listing cannot be transformed.
     */
    abstract fun doObtain(objects: List<S3Object>): T?

    override fun obtain(): T? {
        val request = ListObjectsV2Request.builder().apply {
            bucket(parameters.bucket.get())
            parameters.prefix.orNull?.let { prefix(it) }
        }.build()

        val objects = client.get().listObjectsV2Paginator(request).flatMap { it.contents() }
        return doObtain(objects)
    }
}

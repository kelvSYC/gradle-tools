package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.Object as S3Object
import aws.sdk.kotlin.services.s3.paginators.listObjectsV2Paginated
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource] implementations that produce a value by listing objects in an S3 bucket.
 *
 * Pagination is handled internally via the SDK Kotlin paginated flow; subclasses receive the full list of
 * [S3Object] summaries across all pages via [doObtain] and transform it to the desired type.
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
         * The shared build service managing the S3 client.
         */
        @get:Internal
        val service: Property<S3ClientBuildService>

        /**
         * S3 bucket name.
         */
        val bucket: Property<String>

        /**
         * Optional key prefix used to filter the listing.
         */
        val prefix: Property<String>
    }

    /**
     * Transforms the listed objects into the target type.
     *
     * @param objects All [S3Object] summaries returned across paginated responses.
     * @return The transformed value, or `null` if the listing cannot be transformed.
     */
    abstract fun doObtain(objects: List<S3Object>): T?

    override fun obtain(): T? {
        val request = ListObjectsV2Request {
            bucket = parameters.bucket.get()
            prefix = parameters.prefix.orNull
        }

        return runBlocking {
            val objects = parameters.service.get().getClient().listObjectsV2Paginated(request)
                .toList()
                .flatMap { it.contents.orEmpty() }
            doObtain(objects)
        }
    }
}

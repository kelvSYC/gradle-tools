package com.kelvsyc.gradle.aws.java.s3

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse

/**
 * Base class for [ValueSource] implementations that provide a value by reading a value from AWS.
 *
 * Subclasses should implement the [doObtain] function, transforming a [ResponseBytes] object to an object of the
 * desired type.
 * This class should only be used on S3 objects for which the entire object can be kept in memory.
 */
abstract class AbstractS3ValueSource<T, P : AbstractS3ValueSource.Parameters> : ValueSource<T, P> {
    /**
     * Base parameters interface for [AbstractS3ValueSource]. This contains the data needed to retrieve an object from
     * AWS.
     *
     * Extend this interface if there is a need to supply additional parameters to the [AbstractS3ValueSource]
     * subclass.
     */
    interface Parameters : ValueSourceParameters {
        val client: Property<S3Client>
        val bucket: Property<String>
        val key: Property<String>
    }

    private val request = parameters.bucket.zip(parameters.key) { bucket, key ->
        GetObjectRequest.builder().apply {
            bucket(bucket)
            key(key)
        }.build()
    }

    /**
     * Transforms the data retrieved from AWS.
     *
     * This method is called only once per call to [obtain].
     *
     * @return The data retrieved from AWS, or `null` if the data cannot be transformed into the needed data.
     */
    abstract fun doObtain(content: ResponseBytes<GetObjectResponse>): T?

    override fun obtain(): T? {
        val response = parameters.client.get().getObjectAsBytes(request.get())
        return doObtain(response)
    }
}

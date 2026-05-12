package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * A [WorkAction] that copies a single S3 object from a source bucket/key pair to a destination bucket/key
 * pair. Server-side copy: no data transits through the build machine.
 */
abstract class CopyObjectAction : WorkAction<CopyObjectAction.Parameters> {
    /**
     * Parameters for [CopyObjectAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The shared build service managing the S3 client.
         */
        @get:Internal
        val service: Property<S3ClientBuildService>

        /**
         * Source bucket name.
         */
        val sourceBucket: Property<String>

        /**
         * Source object key.
         */
        val sourceKey: Property<String>

        /**
         * Destination bucket name.
         */
        val destinationBucket: Property<String>

        /**
         * Destination object key.
         */
        val destinationKey: Property<String>
    }

    override fun execute() {
        val src = parameters.sourceBucket.get() + "/" +
            URLEncoder.encode(parameters.sourceKey.get(), StandardCharsets.UTF_8)
        val request = CopyObjectRequest {
            copySource = src
            bucket = parameters.destinationBucket.get()
            key = parameters.destinationKey.get()
        }

        runBlocking {
            parameters.service.get().getClient().copyObject(request)
        }
    }
}

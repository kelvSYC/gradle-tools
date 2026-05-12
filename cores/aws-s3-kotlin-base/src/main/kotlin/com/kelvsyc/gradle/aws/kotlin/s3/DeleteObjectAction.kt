package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * A [WorkAction] that deletes a single S3 object.
 */
abstract class DeleteObjectAction : WorkAction<DeleteObjectAction.Parameters> {
    /**
     * Parameters for [DeleteObjectAction].
     */
    interface Parameters : WorkParameters {
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
         * S3 object key to delete.
         */
        val key: Property<String>
    }

    override fun execute() {
        val request = DeleteObjectRequest {
            bucket = parameters.bucket.get()
            key = parameters.key.get()
        }

        runBlocking {
            parameters.service.get().getClient().deleteObject(request)
        }
    }
}

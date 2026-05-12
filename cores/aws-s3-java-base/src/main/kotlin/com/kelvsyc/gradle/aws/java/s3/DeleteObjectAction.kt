package com.kelvsyc.gradle.aws.java.s3

import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

/**
 * A [WorkAction] that deletes a single S3 object using a synchronous
 * [S3Client][software.amazon.awssdk.services.s3.S3Client].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when deleting
 * multiple objects in a task action.
 */
abstract class DeleteObjectAction : WorkAction<DeleteObjectAction.Parameters> {
    /**
     * Parameters for [DeleteObjectAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the S3 client. */
        val service: Property<S3ClientBuildService>

        /** S3 bucket name. */
        val bucket: Property<String>

        /** S3 object key to delete. */
        val key: Property<String>
    }

    override fun execute() {
        val request = DeleteObjectRequest.builder()
            .bucket(parameters.bucket.get())
            .key(parameters.key.get())
            .build()

        parameters.service.get().getClient().deleteObject(request)
    }
}

package com.kelvsyc.gradle.aws.java.s3

import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.s3.model.CopyObjectRequest

/**
 * A [WorkAction] that copies a single S3 object from a source bucket/key pair to a destination bucket/key
 * pair using a synchronous [S3Client][software.amazon.awssdk.services.s3.S3Client]. Server-side copy: no
 * data transits through the build machine.
 */
abstract class CopyObjectAction : WorkAction<CopyObjectAction.Parameters> {
    /**
     * Parameters for [CopyObjectAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the S3 client. */
        val service: Property<S3ClientBuildService>

        /** Source bucket name. */
        val sourceBucket: Property<String>

        /** Source object key. */
        val sourceKey: Property<String>

        /** Destination bucket name. */
        val destinationBucket: Property<String>

        /** Destination object key. */
        val destinationKey: Property<String>
    }

    override fun execute() {
        val request = CopyObjectRequest.builder()
            .sourceBucket(parameters.sourceBucket.get())
            .sourceKey(parameters.sourceKey.get())
            .destinationBucket(parameters.destinationBucket.get())
            .destinationKey(parameters.destinationKey.get())
            .build()

        parameters.service.get().getClient().copyObject(request)
    }
}

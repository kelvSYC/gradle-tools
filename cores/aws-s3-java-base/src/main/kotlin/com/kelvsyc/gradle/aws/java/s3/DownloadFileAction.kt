package com.kelvsyc.gradle.aws.java.s3

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.model.GetObjectRequest

/**
 * A [WorkAction] that downloads a single S3 object to a local file using a synchronous
 * [S3Client][software.amazon.awssdk.services.s3.S3Client].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution; this gives
 * batch-style throughput without requiring an `S3TransferManager` registration.
 */
abstract class DownloadFileAction : WorkAction<DownloadFileAction.Parameters> {
    /**
     * Parameters for [DownloadFileAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the S3 client. */
        @get:Internal
        val service: Property<S3ClientBuildService>

        /** S3 bucket name. */
        val bucket: Property<String>

        /** S3 object key. */
        val key: Property<String>

        /** Destination file the object is written to. */
        val outputFile: RegularFileProperty
    }

    override fun execute() {
        val request = GetObjectRequest.builder()
            .bucket(parameters.bucket.get())
            .key(parameters.key.get())
            .build()

        parameters.service.get().getClient()
            .getObject(request, ResponseTransformer.toFile(parameters.outputFile.get().asFile))
    }
}

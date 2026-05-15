package com.kelvsyc.gradle.aws.java.s3

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.PutObjectRequest

/**
 * A [WorkAction] that uploads a single local file to S3 using a synchronous
 * [S3Client][software.amazon.awssdk.services.s3.S3Client].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution; this gives
 * batch-style throughput without requiring an `S3TransferManager` registration.
 */
abstract class UploadFileAction : WorkAction<UploadFileAction.Parameters> {
    /**
     * Parameters for [UploadFileAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the S3 client. */
        @get:Internal
        val service: Property<S3ClientBuildService>

        /** S3 bucket name. */
        val bucket: Property<String>

        /** S3 object key. */
        val key: Property<String>

        /** Source file uploaded to S3. */
        val inputFile: RegularFileProperty
    }

    override fun execute() {
        val request = PutObjectRequest.builder()
            .bucket(parameters.bucket.get())
            .key(parameters.key.get())
            .build()

        parameters.service.get().getClient()
            .putObject(request, RequestBody.fromFile(parameters.inputFile.get().asFile))
    }
}

package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

/**
 * A [WorkAction] that uploads a single local file to S3 using a synchronous [S3Client].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution; this gives
 * batch-style throughput without requiring an `S3TransferManager` registration.
 */
abstract class UploadFileAction : WorkAction<UploadFileAction.Parameters> {
    /**
     * Parameters for [UploadFileAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The shared [ClientsBaseService] holding the registered S3 client.
         */
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
         * S3 object key.
         */
        val key: Property<String>

        /**
         * Source file uploaded to S3.
         */
        val inputFile: RegularFileProperty
    }

    private val client: Provider<S3Client> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = PutObjectRequest.builder().apply {
            bucket(parameters.bucket.get())
            key(parameters.key.get())
        }.build()

        client.get().putObject(request, RequestBody.fromFile(parameters.inputFile.get().asFile))
    }
}

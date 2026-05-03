package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

/**
 * A [WorkAction] that downloads a single S3 object to a local file using a synchronous [S3Client].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution; this gives
 * batch-style throughput without requiring an `S3TransferManager` registration.
 */
abstract class DownloadFileAction : WorkAction<DownloadFileAction.Parameters> {
    /**
     * Parameters for [DownloadFileAction].
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
         * Destination file the object is written to.
         */
        val outputFile: RegularFileProperty
    }

    private val client: Provider<S3Client> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = GetObjectRequest.builder().apply {
            bucket(parameters.bucket.get())
            key(parameters.key.get())
        }.build()

        client.get().getObject(request, ResponseTransformer.toFile(parameters.outputFile.get().asFile))
    }
}

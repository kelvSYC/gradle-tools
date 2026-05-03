package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

/**
 * A [WorkAction] that deletes a single S3 object using a synchronous [S3Client].
 *
 * Submit through `WorkerExecutor.noIsolation()` to enable Gradle-managed parallel execution when deleting
 * multiple objects in a task action.
 */
abstract class DeleteObjectAction : WorkAction<DeleteObjectAction.Parameters> {
    /**
     * Parameters for [DeleteObjectAction].
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
         * S3 object key to delete.
         */
        val key: Property<String>
    }

    private val client: Provider<S3Client> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = DeleteObjectRequest.builder().apply {
            bucket(parameters.bucket.get())
            key(parameters.key.get())
        }.build()

        client.get().deleteObject(request)
    }
}

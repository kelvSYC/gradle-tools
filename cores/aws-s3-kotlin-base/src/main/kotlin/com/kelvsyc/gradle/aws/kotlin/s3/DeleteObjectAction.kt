package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
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
        val request = DeleteObjectRequest {
            bucket = parameters.bucket.get()
            key = parameters.key.get()
        }

        runBlocking {
            client.get().deleteObject(request)
        }
    }
}

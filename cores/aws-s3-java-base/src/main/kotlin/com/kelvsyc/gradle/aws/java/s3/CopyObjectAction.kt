package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import org.gradle.api.tasks.Internal

/**
 * A [WorkAction] that copies a single S3 object from a source bucket/key pair to a destination bucket/key
 * pair using a synchronous [S3Client]. Server-side copy: no data transits through the build machine.
 */
abstract class CopyObjectAction : WorkAction<CopyObjectAction.Parameters> {
    /**
     * Parameters for [CopyObjectAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The shared [ClientsBaseService] holding the registered S3 client.
         */
        @get:Internal
        val service: Property<ClientsBaseService>

        /**
         * Registered name of an [S3ClientInfo].
         */
        val clientName: Property<String>

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

    private val client: Provider<S3Client> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = CopyObjectRequest.builder().apply {
            sourceBucket(parameters.sourceBucket.get())
            sourceKey(parameters.sourceKey.get())
            destinationBucket(parameters.destinationBucket.get())
            destinationKey(parameters.destinationKey.get())
        }.build()

        client.get().copyObject(request)
    }
}

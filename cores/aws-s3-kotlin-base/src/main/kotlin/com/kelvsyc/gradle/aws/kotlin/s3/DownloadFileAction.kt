package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.writeToFile
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

abstract class DownloadFileAction : WorkAction<DownloadFileAction.Parameters> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val bucket: Property<String>
        val key: Property<String>

        val outputFile: RegularFileProperty
    }

    private val client: Provider<S3Client> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    private val request = parameters.bucket.zip(parameters.key) { bucket, key ->
        GetObjectRequest {
            this.bucket = bucket
            this.key = key
        }
    }

    override fun execute() {
        runBlocking {
            client.get().getObject(request.get()) {
                it.body?.writeToFile(parameters.outputFile.get().asFile)
            }
        }
    }
}

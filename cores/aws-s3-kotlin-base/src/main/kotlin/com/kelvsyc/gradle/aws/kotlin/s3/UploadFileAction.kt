package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

abstract class UploadFileAction : WorkAction<UploadFileAction.Parameters> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val bucket: Property<String>
        val key: Property<String>

        val inputFile: RegularFileProperty
    }

    private val client: Provider<S3Client> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = PutObjectRequest {
            bucket = parameters.bucket.get()
            key = parameters.key.get()

            body = ByteStream.fromFile(parameters.inputFile.get().asFile)
        }

        runBlocking {
            client.get().putObject(request)
        }
    }
}

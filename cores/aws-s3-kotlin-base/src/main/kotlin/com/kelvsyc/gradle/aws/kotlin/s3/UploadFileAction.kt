package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

abstract class UploadFileAction : WorkAction<UploadFileAction.Parameters> {
    interface Parameters : WorkParameters {
        @get:Internal
        val service: Property<S3ClientBuildService>

        val bucket: Property<String>
        val key: Property<String>

        val inputFile: RegularFileProperty
    }

    override fun execute() {
        val request = PutObjectRequest {
            bucket = parameters.bucket.get()
            key = parameters.key.get()

            body = ByteStream.fromFile(parameters.inputFile.get().asFile)
        }

        runBlocking {
            parameters.service.get().getClient().putObject(request)
        }
    }
}

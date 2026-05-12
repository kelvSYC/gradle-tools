package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

abstract class DownloadFileAction : WorkAction<DownloadFileAction.Parameters> {
    interface Parameters : WorkParameters {
        @get:Internal
        val service: Property<S3ClientBuildService>

        val bucket: Property<String>
        val key: Property<String>

        val outputFile: RegularFileProperty
    }

    private val request = parameters.bucket.zip(parameters.key) { bucket, key ->
        GetObjectRequest {
            this.bucket = bucket
            this.key = key
        }
    }

    override fun execute() {
        runBlocking {
            parameters.service.get().getClient().getObject(request.get()) {
                it.body?.writeToFile(parameters.outputFile.get().asFile)
            }
        }
    }
}

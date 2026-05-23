package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task that downloads an S3 object to a local file.
 *
 * Retrieves an object from an S3 bucket and writes it to a local file. The task output
 * file is tracked by Gradle's output cache, so no @UntrackedTask annotation is needed.
 */
abstract class DownloadFile : DefaultTask() {

    /** The build service managing the S3 client. */
    @get:Internal
    abstract val service: Property<S3ClientBuildService>

    /** S3 bucket name. */
    @get:Input
    abstract val bucket: Property<String>

    /** S3 object key. */
    @get:Input
    abstract val key: Property<String>

    /** Local file to write the downloaded object to. */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val request = bucket.zip(key) { b, k ->
            GetObjectRequest {
                bucket = b
                key = k
            }
        }

        runBlocking {
            service.get().getClient().getObject(request.get()) {
                it.body?.writeToFile(outputFile.get().asFile)
            }
        }
    }
}

package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Task that uploads a local file to an S3 bucket.
 *
 * This task uploads a local file using server-side PutObject. The upload is not cacheable
 * because it communicates with an external service (AWS S3).
 */
@DisableCachingByDefault(because = "Uploading to an external service is not cacheable")
abstract class UploadFile : DefaultTask() {

    /** The build service managing the S3 client. */
    @get:Internal
    abstract val service: Property<S3ClientBuildService>

    /** S3 bucket name. */
    @get:Input
    abstract val bucket: Property<String>

    /** S3 object key. */
    @get:Input
    abstract val key: Property<String>

    /** Local file to upload. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputFile: RegularFileProperty

    @TaskAction
    fun execute() {
        val request = PutObjectRequest {
            bucket = this@UploadFile.bucket.get()
            key = this@UploadFile.key.get()
            body = ByteStream.fromFile(this@UploadFile.inputFile.get().asFile)
        }

        runBlocking {
            service.get().getClient().putObject(request)
        }
    }
}

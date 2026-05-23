package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.CopyObjectRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Task that copies a single S3 object from a source bucket/key pair to a destination bucket/key pair.
 *
 * This is a server-side copy operation: no data transits through the build machine.
 */
@UntrackedTask(because = "Communicates with AWS S3; no local output")
abstract class CopyObject : DefaultTask() {

    /** The build service managing the S3 client. */
    @get:Internal
    abstract val service: Property<S3ClientBuildService>

    /** Source bucket name. */
    @get:Input
    abstract val sourceBucket: Property<String>

    /** Source object key. */
    @get:Input
    abstract val sourceKey: Property<String>

    /** Destination bucket name. */
    @get:Input
    abstract val destinationBucket: Property<String>

    /** Destination object key. */
    @get:Input
    abstract val destinationKey: Property<String>

    @TaskAction
    fun execute() {
        val src = sourceBucket.get() + "/" +
            URLEncoder.encode(sourceKey.get(), StandardCharsets.UTF_8)
        val request = CopyObjectRequest {
            copySource = src
            bucket = destinationBucket.get()
            key = destinationKey.get()
        }

        runBlocking {
            service.get().getClient().copyObject(request)
        }
    }
}

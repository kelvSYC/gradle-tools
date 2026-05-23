package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that deletes a single S3 object.
 */
@UntrackedTask(because = "Communicates with AWS S3; no local output")
abstract class DeleteObject : DefaultTask() {

    /** The build service managing the S3 client. */
    @get:Internal
    abstract val service: Property<S3ClientBuildService>

    /** S3 bucket name. */
    @get:Input
    abstract val bucket: Property<String>

    /** S3 object key to delete. */
    @get:Input
    abstract val key: Property<String>

    @TaskAction
    fun execute() {
        val request = DeleteObjectRequest {
            bucket = this@DeleteObject.bucket.get()
            key = this@DeleteObject.key.get()
        }

        runBlocking {
            service.get().getClient().deleteObject(request)
        }
    }
}

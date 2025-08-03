package com.kelvsyc.gradle.aws.java.s3

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * This task uploads a number of artifacts to S3. Each artifact consists of a bucket and key name, as well as a
 * source file. All artifacts must be supplied with a name for easy references.
 *
 * Specify artifacts to upload using [registerArtifact].
 *
 * All artifacts will be uploaded concurrently. This task fails if any artifact fails to be uploaded.
 */
abstract class AbstractBatchUploadToS3 @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory
) : DefaultTask() {
    /**
     * The [S3TransferManager] client used to perform the download.
     */
    @get:Internal
    abstract val client: Property<S3TransferManager>

    /**
     * Information about an artifact to be retrieved from S3.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        abstract val inputFile: RegularFileProperty
        abstract val bucket: Property<String>
        abstract val key: Property<String>

        @Suppress("LeakingThis")
        internal val putObjectRequest = bucket.zip(key) { bucket, key ->
            PutObjectRequest.builder().apply {
                bucket(bucket)
                key(key)
            }.build()
        }

        @Suppress("LeakingThis")
        internal val uploadFileRequest = putObjectRequest.zip(inputFile) { inner, src ->
            UploadFileRequest.builder().apply {
                putObjectRequest(inner)
                source(src.asFile)
            }.build()
        }
    }

    /**
     * The artifacts to be downloaded.
     *
     * Users generally add to this collection through [registerArtifact].
     */
    @get:Internal
    abstract val artifacts: MapProperty<String, Artifact>

    /**
     * Registers a new artifact to be downloaded.
     */
    fun registerArtifact(name: String, configureAction: Action<in Artifact>) {
        artifacts.put(name, providers.provider {
            objects.newInstance<Artifact>(name).also { configureAction.execute(it) }
        })
    }

    @Suppress("LeakingThis")
    private val requests = artifacts.map {
        it.mapValues { it.value.uploadFileRequest.get() }
    }

    @TaskAction
    fun run() {
        val futures = requests.get().map {
            client.get().uploadFile(it.value).completionFuture()
        }

        @Suppress("detekt:SpreadOperator")
        val allFuture = CompletableFuture.allOf(*futures.toTypedArray()).thenApply {
            futures.map { it.join() }
        }

        allFuture.join()
    }
}

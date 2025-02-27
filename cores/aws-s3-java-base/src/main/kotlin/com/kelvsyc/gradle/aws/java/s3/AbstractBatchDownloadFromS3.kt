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
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * This task retrieves a number of artifacts from S3. Each artifact consists of a bucket and key name, as well as a
 * download destination. All artifacts must be supplied with a name for easy references.
 *
 * Specify artifacts to download using [registerArtifact].
 *
 * All artifacts will be downloaded concurrently. This task fails if any artifact fails to be retrieved.
 */
abstract class AbstractBatchDownloadFromS3 @Inject constructor(
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
    abstract class Artifact(private val name: String) : Named {
        override fun getName() = name

        abstract val bucket: Property<String>
        abstract val key: Property<String>
        abstract val outputFile: RegularFileProperty

        @Suppress("LeakingThis")
        internal val getObjectRequest = bucket.zip(key) { bucket, key ->
            GetObjectRequest.builder().apply {
                bucket(bucket)
                key(key)
            }.build()
        }

        @Suppress("LeakingThis")
        internal val downloadFileRequest = getObjectRequest.zip(outputFile) { inner, dest ->
            DownloadFileRequest.builder().apply {
                getObjectRequest(inner)
                destination(dest.asFile)
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
        it.mapValues { it.value.downloadFileRequest.get() }
    }

    /**
     * The locations of the downloaded artifacts.
     *
     * This property is provided as a convenience for wiring the output of this task to other task inputs.
     */
    @Suppress("LeakingThis")
    @get:OutputFiles
    val outputFiles = artifacts.map { it.mapValues { it.value.outputFile.get() } }

    @TaskAction
    fun run() {
        val futures = requests.get().map {
            client.get().downloadFile(it.value).completionFuture()
        }

        @Suppress("detekt:SpreadOperator")
        val allFuture = CompletableFuture.allOf(*futures.toTypedArray()).thenApply {
            futures.map { it.join() }
        }

        allFuture.join()
    }
}

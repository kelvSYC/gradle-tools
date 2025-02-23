package com.kelvsyc.gradle.google.cloud.storage

import com.google.cloud.BatchResult
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Named
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * This task retrieves a number of artifacts from GCS. Each artifact consists of a bucket and blob name, as well as a
 * download destination. All artifacts must be supplied with a name for easy references.
 *
 * Specify artifacts to download using [registerArtifact].
 *
 * All registered artifacts will be retrieved in a single call to GCS using its batching feature. This task fails if
 * any artifact fails to be retrieved.
 */
abstract class AbstractBatchDownloadFromGCS @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory
) : DefaultTask() {
    /**
     * The [Storage] client used to perform the download.
     */
    @get:Internal
    abstract val client: Property<Storage>

    /**
     * Information about an artifact to be retrieved from storage.
     */
    abstract class Artifact(private val name: String) : Named {
        override fun getName() = name

        abstract val bucket: Property<String>
        abstract val blobName: Property<String>
        abstract val outputFile: RegularFileProperty

        @Suppress("LeakingThis")
        val blobId = bucket.zip(blobName, BlobId::of)
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

    private class Callback(
        private val artifactName: String,
        private val outputFile: RegularFile
    ) : BatchResult.Callback<Blob, StorageException> {
        companion object {
            private val logger = Logging.getLogger(Callback::class.java)
        }

        var successful by Delegates.notNull<Boolean>()
        var error: StorageException? = null

        override fun success(blob: Blob?) {
            if (blob != null) {
                blob.downloadTo(outputFile.asFile.toPath())
                logger.lifecycle("Written artifact '{}' to {}", artifactName, outputFile.asFile.absolutePath)
                successful = true
            } else {
                logger.lifecycle("Failed to retrieve artifact {} from GCS; blob not found", artifactName)
                successful = false
            }
        }

        override fun error(e: StorageException) {
            logger.lifecycle("Failed to retrieve artiface '$artifactName' from GCS", e)
            successful = false
            error = e
        }
    }

    /**
     * The locations of the downloaded artifacts.
     *
     * This property is provided as a convenience for wiring the output of this task to other task inputs.
     */
    @Suppress("LeakingThis")
    @get:OutputFiles
    val outputFiles = artifacts.map { it.mapValues { it.value.outputFile.get() } }

    @Suppress("LeakingThis")
    private val batch = artifacts.map {
        val data = it.mapValues { it.value.blobId.get() to Callback(it.key, it.value.outputFile.get()) }

        val batch = client.get().batch().apply {
            data.forEach {
                val (blob, callback) = it.value
                get(blob).notify(callback)
            }
        }
        val callbacks = data.mapValues { it.value.second }
        batch to callbacks
    }

    @TaskAction
    fun run() {
        val (request, callbacks) = batch.get()
        request.submit()

        val failed = callbacks.filter { !it.value.successful }
        if (failed.isNotEmpty()) {
            // TODO detailed error handling
            throw GradleException("Failed to retrieve some artifacts")
        }
    }
}

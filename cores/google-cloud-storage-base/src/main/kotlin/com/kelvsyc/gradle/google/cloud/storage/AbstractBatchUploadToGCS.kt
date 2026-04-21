package com.kelvsyc.gradle.google.cloud.storage

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * This task uploads a number of artifacts to GCS. Each artifact consists of a bucket and blob name, as well as a
 * source file. All artifacts must be supplied with a name for easy references.
 *
 * Specify artifacts to upload using [registerArtifact].
 *
 * All artifacts will be uploaded using [UploadFileAction]. This task fails if any artifact fails to be uploaded.
 */
@DisableCachingByDefault(because = "Uploading to an external service is not cacheable")
abstract class AbstractBatchUploadToGCS @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {
    /**
     * The [ClientsBaseService] used to obtain the GCS client.
     */
    @get:Internal
    abstract val service: Property<ClientsBaseService>

    /**
     * Registered name of a [StorageClientInfo].
     */
    @get:Internal
    abstract val clientName: Property<String>

    /**
     * Information about an artifact to be uploaded to GCS.
     */
    abstract class Artifact @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        @get:Internal
        abstract val bucket: Property<String>

        @get:Internal
        abstract val blobName: Property<String>

        @get:InputFiles
        @get:PathSensitive(PathSensitivity.NONE)
        abstract val inputFile: RegularFileProperty
    }

    /**
     * The artifacts to be uploaded.
     *
     * Users generally add to this collection through [registerArtifact].
     */
    @get:Internal
    abstract val artifacts: MapProperty<String, Artifact>

    /**
     * Registers a new artifact to be uploaded.
     */
    fun registerArtifact(name: String, configureAction: Action<in Artifact>) {
        artifacts.put(name, providers.provider {
            objects.newInstance<Artifact>(name).also { configureAction.execute(it) }
        })
    }

    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        artifacts.get().forEach { (_, artifact) ->
            queue.submit(UploadFileAction::class.java) {
                service.set(this@AbstractBatchUploadToGCS.service)
                clientName.set(this@AbstractBatchUploadToGCS.clientName)
                bucket.set(artifact.bucket)
                blobName.set(artifact.blobName)
                inputFile.set(artifact.inputFile)
            }
        }
    }
}

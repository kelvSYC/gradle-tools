package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.services.codeartifact.model.PackageFormat
import software.amazon.awssdk.services.codeartifact.model.PublishPackageVersionRequest
import javax.inject.Inject

/**
 * Abstract root [DefaultTask] that publishes multiple assets to a CodeArtifact generic repository.
 *
 * Handles artifact registration and request building. Subclasses implement [doExecute] to provide
 * the actual upload concurrency model.
 *
 * **`unfinished` flag:** When uploading multiple assets to the same package version (e.g., a jar and its
 * sources jar), set `unfinished = true` on all but the last asset. CodeArtifact marks the version as
 * `Unfinished` until an asset is published with `unfinished = false` (or absent). Because uploads run
 * concurrently, all assets in a batch will be uploaded in parallel — the ordering of `unfinished` flags
 * across a batch is unpredictable. Use separate batch tasks (or single-asset tasks) when ordering matters.
 *
 * **Choosing a subclass:**
 * - Extend [AbstractWorkerBatchPublishPackageVersion] (or use [BatchPublishPackageVersion])
 *   for a Worker API-based approach using the synchronous `CodeartifactClient`.
 * - Extend [AbstractAsyncBatchPublishPackageVersion] (or use [AsyncBatchPublishPackageVersion])
 *   for a `CompletableFuture`-based approach using the asynchronous `CodeartifactAsyncClient`.
 *
 * **BYO-client use:** Extend one of the abstract mid-level classes and set the `service` or `client`
 * property directly rather than using the service-wired concrete classes.
 *
 * @see AbstractWorkerBatchPublishPackageVersion
 * @see AbstractAsyncBatchPublishPackageVersion
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class AbstractBatchPublishPackageVersion @Inject constructor(
    private val objects: ObjectFactory,
) : DefaultTask() {

    /**
     * Per-artifact coordinates and content for publishing a CodeArtifact generic package version asset.
     */
    interface Artifact {
        /** The CodeArtifact domain name. */
        @get:Input
        val domain: Property<String>

        /** The 12-digit account number of the domain owner. */
        @get:Input
        val domainOwner: Property<String>

        /** The CodeArtifact repository name. */
        @get:Input
        val repository: Property<String>

        /** The package namespace. */
        @get:Input
        val namespace: Property<String>

        /** The package name. */
        @get:Input
        val packageValue: Property<String>

        /** The package version. */
        @get:Input
        val packageVersion: Property<String>

        /** The asset name within the package version. */
        @get:Input
        val assetName: Property<String>

        /** The SHA-256 hash of the asset content. */
        @get:Input
        val assetSHA256: Property<String>

        /** The asset file to upload. */
        @get:InputFile
        @get:PathSensitive(PathSensitivity.NONE)
        val assetContent: RegularFileProperty

        /**
         * Whether the package version should remain in the `Unfinished` state after publishing this asset.
         *
         * Set to `true` when uploading multiple assets to the same package version. When `false` or unset,
         * CodeArtifact marks the version as finished after this asset is published.
         */
        @get:Input
        @get:Optional
        val unfinished: Property<Boolean>
    }

    /** Map of artifact name to per-artifact configuration. */
    @get:Nested
    abstract val artifacts: MapProperty<String, Artifact>

    /**
     * Registers an artifact to publish.
     *
     * @param name Unique name for this artifact within the batch (used in log messages and error reporting).
     * @param action Configures the artifact's coordinates, content, and checksum.
     */
    fun registerArtifact(name: String, action: Action<in Artifact>) {
        val artifact = objects.newInstance<Artifact>().also { action.execute(it) }
        artifacts.put(name, artifact)
    }

    internal data class Request(
        val name: String,
        val request: PublishPackageVersionRequest,
        val assetContent: RegularFileProperty,
    )

    @Suppress("LeakingThis")
    @get:Internal
    internal val requests = artifacts.map {
        it.map { (artifactName, artifact) ->
            val request = PublishPackageVersionRequest.builder().apply {
                domain(artifact.domain.get())
                domainOwner(artifact.domainOwner.get())
                repository(artifact.repository.get())
                format(PackageFormat.GENERIC)
                namespace(artifact.namespace.get())
                packageValue(artifact.packageValue.get())
                packageVersion(artifact.packageVersion.get())
                assetName(artifact.assetName.get())
                assetSHA256(artifact.assetSHA256.get())
                if (artifact.unfinished.isPresent) {
                    unfinished(artifact.unfinished.get())
                }
            }.build()
            Request(artifactName, request, artifact.assetContent)
        }
    }

}

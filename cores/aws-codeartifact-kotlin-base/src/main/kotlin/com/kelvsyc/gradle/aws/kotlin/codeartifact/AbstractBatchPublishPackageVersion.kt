package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.runtime.ClientException
import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.sdk.kotlin.services.codeartifact.model.PublishPackageVersionRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import com.kelvsyc.gradle.logging.warn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.runBlocking
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Abstract [DefaultTask] that publishes multiple assets to a CodeArtifact generic repository in parallel.
 *
 * Each artifact is registered via [registerArtifact] with its own coordinates, asset content, and checksum.
 * Uploads run concurrently using coroutine `flatMapMerge`. Transient failures (non-[ClientException]) are
 * retried up to [retries] times; [ClientException] failures are not retried. If any upload fails after all
 * retries, the task throws [GradleException] listing the failed artifact names.
 *
 * **`unfinished` flag:** When uploading multiple assets to the same package version (e.g., a jar and its
 * sources jar), set `unfinished = true` on all but the last asset. CodeArtifact marks the version as
 * `Unfinished` until an asset is published with `unfinished = false` (or absent).
 *
 * **BYO-client use:** Set [client] directly and leave [retries] at its default (1) or configure as needed.
 * For service-wired use, extend [BatchPublishPackageVersion] instead.
 *
 * @see BatchPublishPackageVersion
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

    /**
     * The CodeArtifact client to use for uploads.
     * Set directly for BYO-client usage; [BatchPublishPackageVersion] wires this from a build service.
     */
    @get:Internal
    abstract val client: Property<CodeartifactClient>

    /**
     * Maximum number of retry attempts per artifact on transient failures. Defaults to 1.
     * [ClientException] failures are never retried regardless of this value.
     */
    @get:Internal
    abstract val retries: Property<Int>

    internal data class Request(
        val name: String,
        val request: PublishPackageVersionRequest,
    )

    @Suppress("LeakingThis")
    @get:Internal
    internal val requests = artifacts.map {
        it.map { (artifactName, artifact) ->
            val request = PublishPackageVersionRequest {
                domain = artifact.domain.get()
                domainOwner = artifact.domainOwner.get()
                repository = artifact.repository.get()
                format = PackageFormat.Generic
                namespace = artifact.namespace.get()
                `package` = artifact.packageValue.get()
                packageVersion = artifact.packageVersion.get()
                assetName = artifact.assetName.get()
                assetSha256 = artifact.assetSHA256.get()
                assetContent = artifact.assetContent.get().asFile.asByteStream()
                if (artifact.unfinished.isPresent) {
                    unfinished = artifact.unfinished.get()
                }
            }
            Request(artifactName, request)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @TaskAction
    fun run() {
        val results = runBlocking {
            requests.getOrElse(emptyList()).asFlow()
                .flowOn(Dispatchers.IO)
                .onStart {
                    logger.lifecycle("Starting batch publish to CodeArtifact")
                }
                .onEach {
                    logger.lifecycle(
                        "Publishing {} to {}/{}/{}",
                        it.name,
                        it.request.domain,
                        it.request.repository,
                        it.request.assetName,
                    )
                }
                .flatMapMerge { req ->
                    val name = req.name
                    flow {
                        client.get().publishPackageVersion(req.request)
                        emit(name to Result.success(Unit))
                    }.catch {
                        if (it is ClientException) {
                            emit(name to Result.failure(it))
                        }
                    }.retryWhen { cause, attempt ->
                        val totalRetries = retries.getOrElse(1)
                        logger.warn(cause) {
                            "Attempt $attempt of $totalRetries to publish $name to ${req.request.domain}/${req.request.repository}/${req.request.assetName} has failed"
                        }
                        attempt < totalRetries
                    }.catch { cause ->
                        logger.warn(cause) {
                            "Publishing $name to ${req.request.domain}/${req.request.repository}/${req.request.assetName} has failed"
                        }
                        emit(name to Result.failure(cause))
                    }
                }
                .fold(mutableMapOf<String, Result<Unit>>()) { map, result ->
                    map[result.first] = result.second
                    map
                }
        }

        val failures = results.filterValues { it.isFailure }
        if (failures.isNotEmpty()) {
            throw GradleException("${failures.size} artifact(s) failed to publish: ${failures.keys.joinToString()}")
        }
    }
}

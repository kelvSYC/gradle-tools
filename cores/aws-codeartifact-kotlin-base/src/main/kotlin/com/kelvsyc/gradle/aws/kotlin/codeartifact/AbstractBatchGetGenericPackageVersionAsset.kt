package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.runtime.ClientException
import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetPackageVersionAssetRequest
import aws.sdk.kotlin.services.codeartifact.model.PackageFormat
import aws.smithy.kotlin.runtime.content.writeToFile
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
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Abstract [DefaultTask] that downloads multiple assets from a CodeArtifact generic repository in parallel.
 *
 * Each artifact is registered via [registerArtifact] with its own coordinates and destination file. Downloads
 * run concurrently using coroutine `flatMapMerge`. Transient failures (non-[ClientException]) are retried up
 * to [retries] times; [ClientException] (SDK-side errors) and CodeArtifact service errors are not retried.
 * If any download fails after all retries, the task throws [GradleException] listing the failed artifact names.
 *
 * **BYO-client use:** Set [client] directly and leave [retries] at its default (1) or configure as needed.
 * For service-wired use, extend [BatchGetGenericPackageVersionAsset] instead.
 *
 * **Wiring output files:**
 * ```kotlin
 * val myArtifactFile: Provider<RegularFile> = myTask.flatMap {
 *     it.outputFileForArtifact("myArtifact")
 * }
 * ```
 *
 * @see BatchGetGenericPackageVersionAsset
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class AbstractBatchGetGenericPackageVersionAsset @Inject constructor(
    private val objects: ObjectFactory,
) : DefaultTask() {

    /**
     * Per-artifact coordinates and output location for a CodeArtifact generic package version asset.
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

        /** The location the asset is to be downloaded to. */
        @get:OutputFile
        val outputFile: RegularFileProperty
    }

    /** Map of artifact name to per-artifact configuration. */
    @get:Nested
    abstract val artifacts: MapProperty<String, Artifact>

    /**
     * Registers an artifact to download.
     *
     * @param name Unique name for this artifact within the batch (used in log messages and error reporting).
     * @param action Configures the artifact's coordinates and output file.
     */
    fun registerArtifact(name: String, action: Action<in Artifact>) {
        val artifact = objects.newInstance<Artifact>().also { action.execute(it) }
        artifacts.put(name, artifact)
    }

    /**
     * Returns a [Provider] resolving to the output file for the named artifact.
     *
     * Use this to wire the downloaded file as an input to a downstream task without forcing evaluation.
     */
    fun outputFileForArtifact(name: String): Provider<RegularFile> =
        artifacts.getting(name).flatMap { it.outputFile }

    /**
     * The CodeArtifact client to use for downloads.
     * Set directly for BYO-client usage; [BatchGetGenericPackageVersionAsset] wires this from a build service.
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
        val request: GetPackageVersionAssetRequest,
        val output: Provider<RegularFile>,
    )

    @Suppress("LeakingThis")
    @get:Internal
    internal val requests = artifacts.map {
        it.map { (artifactName, artifact) ->
            val request = GetPackageVersionAssetRequest {
                domain = artifact.domain.get()
                domainOwner = artifact.domainOwner.get()
                repository = artifact.repository.get()
                format = PackageFormat.Generic
                namespace = artifact.namespace.get()
                `package` = artifact.packageValue.get()
                packageVersion = artifact.packageVersion.get()
                asset = artifact.assetName.get()
            }
            Request(artifactName, request, artifact.outputFile)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @TaskAction
    fun run() {
        val results = runBlocking {
            requests.getOrElse(emptyList()).asFlow()
                .flowOn(Dispatchers.IO)
                .onStart {
                    logger.lifecycle("Starting batch download from CodeArtifact")
                }
                .onEach {
                    logger.lifecycle(
                        "Downloading {} from {}/{}/{}",
                        it.name,
                        it.request.domain,
                        it.request.repository,
                        it.request.asset,
                    )
                }
                .flatMapMerge { req ->
                    val name = req.name
                    flow {
                        client.get().getPackageVersionAsset(req.request) { response ->
                            response.asset?.writeToFile(req.output.get().asFile)
                        }
                        emit(name to Result.success(Unit))
                    }.catch {
                        if (it is ClientException) {
                            emit(name to Result.failure(it))
                        }
                    }.retryWhen { cause, attempt ->
                        val totalRetries = retries.getOrElse(1)
                        logger.warn(cause) {
                            "Attempt $attempt of $totalRetries to download $name from ${req.request.domain}/${req.request.repository}/${req.request.asset} has failed"
                        }
                        attempt < totalRetries
                    }.catch { cause ->
                        logger.warn(cause) {
                            "Downloading $name from ${req.request.domain}/${req.request.repository}/${req.request.asset} has failed"
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
            throw GradleException("${failures.size} artifact(s) failed to download: ${failures.keys.joinToString()}")
        }
    }
}

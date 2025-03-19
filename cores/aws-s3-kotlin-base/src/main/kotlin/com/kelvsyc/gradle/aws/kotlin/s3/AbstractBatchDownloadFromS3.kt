package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.runtime.ClientException
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ChecksumMode
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import aws.smithy.kotlin.runtime.content.writeToFile
import com.kelvsyc.gradle.logging.warn
import com.kelvsyc.gradle.providers.asAbsolutePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.runBlocking
import org.gradle.api.Action
import org.gradle.api.DefaultTask
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
import javax.inject.Inject

abstract class AbstractBatchDownloadFromS3 @Inject constructor(private val objects: ObjectFactory) : DefaultTask() {
    interface Artifact {
        @get:Input
        val bucket: Property<String>

        @get:Input
        val key: Property<String>

        @get:OutputFile
        val outputFile: RegularFileProperty
    }

    @get:Nested
    abstract val artifacts: MapProperty<String, Artifact>

    fun registerArtifact(name: String, action: Action<in Artifact>) {
        val artifact = objects.newInstance<Artifact>().also { action.execute(it) }
        artifacts.put(name, artifact)
    }

    fun outputFileForArtifact(name: String) = artifacts.getting(name).flatMap { it.outputFile }

    @get:Internal
    abstract val client: Property<S3Client>

    @get:Internal
    abstract val retries: Property<Int>

    /**
     * Determines whether to perform checksum validation on the downloaded items, if the object being downloaded was
     * originally uploaded with a checksum.
     *
     * Checksum validation will not be performed by default, or if the object being downloaded was not originally
     * uploaded with a checksum.
     */
    @get:Internal
    abstract val enableChecksumMode: Property<Boolean>

    @Suppress("LeakingThis", "UnstableApiUsage")
    private val checksumModeInternal = enableChecksumMode.filter { it }.map { ChecksumMode.Enabled}

    internal data class Request(val name: String, val request: GetObjectRequest, val output: Provider<RegularFile>)

    @Suppress("LeakingThis")
    @get:Internal
    internal val requests = artifacts.map {
        it.map {
            val request = GetObjectRequest {
                bucket = it.value.bucket.get()
                key = it.value.key.get()

                checksumMode = checksumModeInternal.orNull
            }
            Request(it.key, request, it.value.outputFile)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @TaskAction
    fun run() {
        // Block returns Map<String, Result<GetObjectResponse> if additional logging is needed
        runBlocking {
            requests.getOrElse(emptyList()).asFlow()
                .flowOn(Dispatchers.IO)
                .onStart {
                    logger.lifecycle("Starting download from S3")
                }
                .onEach {
                    logger.lifecycle("Downloading {} from {} to {}", it.name, "${it.request.bucket}/${it.request.key}", it.output.asAbsolutePath.get())
                }
                .flatMapLatest {
                    // Because we need to have the name preserved, we use flatMapLatest with a flow that emits only one
                    // value, and all of the logic is found within
                    val name = it.name
                    flow {
                        val response = client.get().getObject(it.request) { response ->
                            response.body?.writeToFile(it.output.get().asFile)

                            // Return the response in the absence of anything interesting
                            response
                        }
                        emit(name to Result.success(response))
                    }.catch {
                        // emit non-retriable failure results here
                        if (it is ClientException) {
                            // ClientException means that something happened on our end, so we should not retry on these
                            emit(name to Result.failure(it))
                        }
                    }.retryWhen { cause, attempt ->
                        val totalRetries = retries.getOrElse(1)
                        logger.warn(cause) {
                            "Attempt $attempt of $totalRetries to download $name from ${it.request.bucket}/${it.request.key} to ${it.output.asAbsolutePath.get()} has failed"
                        }
                        attempt < totalRetries
                    }.catch { cause ->
                        logger.warn(cause) {
                            "Downloading $name from ${it.request.bucket}/${it.request.key} to ${it.output.asAbsolutePath.get()} has failed"
                        }
                        emit(name to Result.failure(cause))
                    }
                }
                .fold(mutableMapOf<String, Result<GetObjectResponse>>()) { map, result ->
                    map[result.first] = result.second
                    map
                }
        }
    }
}

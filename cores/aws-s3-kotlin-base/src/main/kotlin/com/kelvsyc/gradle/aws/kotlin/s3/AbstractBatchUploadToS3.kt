package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.runtime.ClientException
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ChecksumAlgorithm
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectResponse
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.fromFile
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
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

abstract class AbstractBatchUploadToS3 @Inject constructor(private val objects: ObjectFactory) : DefaultTask() {
    interface Artifact {
        @get:Input
        val bucket: Property<String>

        @get:Input
        val key: Property<String>

        @get:InputFile
        val inputFile: RegularFileProperty
    }

    @get:Nested
    abstract val artifacts: MapProperty<String, Artifact>

    fun registerArtifact(name: String, action: Action<in Artifact>) {
        val artifact = objects.newInstance<Artifact>().also { action.execute(it) }
        artifacts.put(name, artifact)
    }

    @get:Internal
    abstract val retries: Property<Int>

    /**
     * Determines whether to perform checksum verification on each upload. All registered artifacts will use the
     * sepcified algorithm for uploads.
     */
    @get:Internal
    abstract val checksumAlgorithm: Property<ChecksumAlgorithm>

    @get:Internal
    abstract val client: Property<S3Client>

    internal data class Request(val name: String, val request: PutObjectRequest, val inputFile: Provider<RegularFile>)

    @Suppress("LeakingThis")
    @get:Internal
    internal val requests = artifacts.map {
        it.map {
            val request = PutObjectRequest {
                bucket = it.value.bucket.get()
                key = it.value.key.get()

                checksumAlgorithm = this@AbstractBatchUploadToS3.checksumAlgorithm.orNull

                body = ByteStream.fromFile(it.value.inputFile.asFile.get())
            }

            Request(it.key, request, it.value.inputFile.locationOnly)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @TaskAction
    fun run() {
        runBlocking {
            requests.getOrElse(emptyList()).asFlow()
                .flowOn(Dispatchers.IO)
                .onStart {
                    logger.lifecycle("Starting upload to S3")
                }
                .onEach {
                    logger.lifecycle("Upload {} to {} from {}", it.name, "${it.request.bucket}/${it.request.key}", it.inputFile.asAbsolutePath.get())
                }
                .flatMapLatest {
                    val name = it.name
                    flow {
                        val response = client.get().putObject(it.request)
                        emit(name to Result.success(response))
                    }.catch {
                        // emit non-retriable failure results here
                        if (it is ClientException) {
                            // ClientExceptions mean that something happened on our end, so we should not retry on those
                            emit(name to Result.failure(it))
                        }
                    }.retryWhen { cause, attempt ->
                        val totalRetries = retries.getOrElse(1)
                        logger.warn(cause) {
                            "Attempt $attempt of $totalRetries to upload $name to ${it.request.bucket}/${it.request.key} from ${it.inputFile.asAbsolutePath.get()} has failed"
                        }
                        attempt < totalRetries
                    }.catch {  cause ->
                        // All non-retriable errors and all errors from exhaustion of retries will be caught here
                        logger.warn(cause) {
                            "Uploading $name to ${it.request.bucket}/${it.request.key} from ${it.inputFile.asAbsolutePath.get()} has failed"
                        }
                        emit(name to Result.failure(cause))
                    }
                }
                .fold(mutableMapOf<String, Result<PutObjectResponse>>()) { map, result ->
                    map[result.first] = result.second
                    map
                }
        }
    }
}

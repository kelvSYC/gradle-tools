package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.UpdateFunctionCodeRequest
import aws.sdk.kotlin.services.lambda.waiters.waitUntilFunctionUpdatedV2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
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
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Abstract [DefaultTask] that updates the deployment package for multiple Lambda functions in parallel.
 *
 * Each function is registered via [registerArtifact] with its own zip file. Updates run concurrently
 * using coroutine `flatMapMerge`. If any update fails the exception propagates and the task fails.
 *
 * When [waitForActive] is `true` (the default), the task waits for each function's `LastUpdateStatus`
 * to reach `Successful` before completing, preventing downstream tasks from racing a function that is
 * still activating its new code.
 *
 * When [versionArnsFile] is configured, the task writes a JSON map of `{functionName → versionArn}`
 * to that file after all updates complete. Only functions where [Artifact.publish] is `true` and the
 * response contains a function ARN are included. The file is always written when configured.
 *
 * **BYO-client use:** Set [client] directly. For service-wired use, extend [BatchUpdateFunctionCode] instead.
 *
 * @see BatchUpdateFunctionCode
 */
@DisableCachingByDefault(because = "Communicates with AWS Lambda")
abstract class AbstractBatchUpdateFunctionCode @Inject constructor(
    private val objects: ObjectFactory,
) : DefaultTask() {

    /**
     * Per-function coordinates and deployment package for a single Lambda code update.
     */
    interface Artifact {
        /** The function name, ARN, or partial ARN to update. */
        @get:Input
        val functionName: Property<String>

        /** Path to the deployment package zip file to upload. */
        @get:InputFile
        @get:PathSensitive(PathSensitivity.NONE)
        val zipFile: RegularFileProperty

        /** Whether to publish a new version after the update. When absent, defaults to `false`. */
        @get:Input
        @get:Optional
        val publish: Property<Boolean>
    }

    /** Map of artifact name to per-function configuration. */
    @get:Nested
    abstract val artifacts: MapProperty<String, Artifact>

    /**
     * Registers a function to update.
     *
     * @param name Unique name for this artifact within the batch.
     * @param action Configures the function name, zip file, and publish flag.
     */
    fun registerArtifact(name: String, action: Action<in Artifact>) {
        val artifact = objects.newInstance<Artifact>().also { action.execute(it) }
        artifacts.put(name, artifact)
    }

    /**
     * The Lambda client to use for updates.
     * Set directly for BYO-client usage; [BatchUpdateFunctionCode] wires this from a build service.
     */
    @get:Internal
    abstract val client: Property<LambdaClient>

    /**
     * Whether to wait for each function's `LastUpdateStatus` to reach `Successful` after uploading.
     *
     * Defaults to `true`. Set to `false` to skip the waiter and return as soon as the upload API call
     * completes. Skipping may cause downstream tasks that invoke or configure the function to race the update.
     */
    @get:Internal
    abstract val waitForActive: Property<Boolean>

    /**
     * Optional file to receive the `{functionName → versionArn}` JSON map after all updates complete.
     *
     * Only populated for functions where [Artifact.publish] is `true` and the response contains an ARN.
     * The file is always written when configured (empty JSON `{}` if no functions were published).
     */
    @get:OutputFile
    @get:Optional
    abstract val versionArnsFile: RegularFileProperty

    internal data class Request(
        val name: String,
        val functionName: String,
        val zipFile: RegularFileProperty,
        val publish: Boolean?,
    )

    @Suppress("LeakingThis")
    @get:Internal
    internal val requests = artifacts.map { map ->
        map.entries.map { (artifactName, artifact) ->
            Request(
                name = artifactName,
                functionName = artifact.functionName.get(),
                zipFile = artifact.zipFile,
                publish = artifact.publish.orNull,
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @TaskAction
    fun run() {
        val versionArns = ConcurrentHashMap<String, String>()
        runBlocking {
            requests.getOrElse(emptyList()).asFlow()
                .flatMapMerge { req ->
                    flow {
                        val bytes = req.zipFile.get().asFile.readBytes()
                        val request = UpdateFunctionCodeRequest {
                            functionName = req.functionName
                            zipFile = bytes
                            publish = req.publish
                        }
                        val response = client.get().updateFunctionCode(request)
                        if (waitForActive.getOrElse(true)) {
                            client.get().waitUntilFunctionUpdatedV2 {
                                functionName = req.functionName
                            }
                        }
                        if (req.publish == true && response.functionArn != null) {
                            versionArns[req.functionName] = response.functionArn!!
                        }
                        emit(Unit)
                    }
                }
                .collect()
        }
        if (versionArnsFile.isPresent) {
            writeVersionArnsJson(versionArnsFile.get().asFile, versionArns)
        }
    }

    private fun writeVersionArnsJson(file: File, arns: Map<String, String>) {
        val json = buildString {
            append("{")
            arns.entries.forEachIndexed { i, (name, arn) ->
                if (i > 0) append(",")
                append("\n    \"$name\": \"$arn\"")
            }
            if (arns.isNotEmpty()) append("\n")
            append("}")
        }
        file.writeText(json)
    }
}

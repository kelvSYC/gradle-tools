package com.kelvsyc.gradle.aws.java.lambda

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
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import java.io.File
import javax.inject.Inject

/**
 * Abstract root [DefaultTask] that updates the deployment package for multiple Lambda functions.
 *
 * Handles artifact registration and request parameter building. Subclasses provide their own
 * `@TaskAction` to implement the actual update using either a synchronous or asynchronous client.
 *
 * **Choosing a subclass:**
 * - Extend [AbstractSyncBatchUpdateFunctionCode] (or use [BatchUpdateFunctionCode]) for direct synchronous
 *   calls using a `LambdaClient`. Updates are executed sequentially.
 * - Extend [AbstractAsyncBatchUpdateFunctionCode] (or use [AsyncBatchUpdateFunctionCode]) for concurrent
 *   `CompletableFuture`-based calls using a `LambdaAsyncClient`. All updates run concurrently.
 *
 * **BYO-client use:** Extend one of the abstract mid-level classes and set the `service` or `client`
 * property directly rather than using the service-wired concrete classes.
 *
 * @see AbstractSyncBatchUpdateFunctionCode
 * @see AbstractAsyncBatchUpdateFunctionCode
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

    internal fun writeVersionArnsJson(file: File, arns: Map<String, String>) {
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

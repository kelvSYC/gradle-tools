package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.providers.asPath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.codeartifact.CodeartifactAsyncClient
import java.util.concurrent.CompletableFuture

/**
 * Abstract [AbstractBatchGetGenericPackageVersionAsset] that uses a [CodeartifactAsyncClient] to download
 * multiple assets concurrently via `CompletableFuture`.
 *
 * All downloads are initiated immediately and joined at the end of the task action. There is no built-in
 * retry support — failures propagate immediately. If any future fails, the task throws.
 *
 * **BYO-client use:** Extend this class and set [client] directly. For automatic service registration via
 * `@ServiceReference`, extend [AsyncBatchGetGenericPackageVersionAsset] instead.
 *
 * @see AsyncBatchGetGenericPackageVersionAsset
 * @see AbstractWorkerBatchGetGenericPackageVersionAsset
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class AbstractAsyncBatchGetGenericPackageVersionAsset : AbstractBatchGetGenericPackageVersionAsset() {

    /**
     * The asynchronous CodeArtifact client.
     * Set directly for BYO-client usage; [AsyncBatchGetGenericPackageVersionAsset] wires this from a build service.
     */
    @get:Internal
    abstract val client: Property<CodeartifactAsyncClient>

    @Suppress("detekt:SpreadOperator")
    @TaskAction
    fun run() {
        val futures = requests.getOrElse(emptyList()).map { req ->
            client.get()
                .getPackageVersionAsset(req.request, AsyncResponseTransformer.toFile(req.outputFile.asPath.get()))
                .thenApply { Unit }
        }
        CompletableFuture.allOf(*futures.toTypedArray<CompletableFuture<Unit>>()).join()
    }
}

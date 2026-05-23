package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.providers.asPath
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.codeartifact.CodeartifactAsyncClient
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

/**
 * Abstract [AbstractBatchPublishPackageVersion] that uses a [CodeartifactAsyncClient] to publish
 * multiple assets concurrently via `CompletableFuture`.
 *
 * All uploads are initiated immediately and joined at the end of the task action. There is no built-in
 * retry support — failures propagate immediately. If any future fails, the task throws.
 *
 * **BYO-client use:** Extend this class and set [client] directly. For automatic service registration via
 * `@ServiceReference`, extend [AsyncBatchPublishPackageVersion] instead.
 *
 * @see AsyncBatchPublishPackageVersion
 * @see AbstractWorkerBatchPublishPackageVersion
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class AbstractAsyncBatchPublishPackageVersion @Inject constructor(
    objects: ObjectFactory,
) : AbstractBatchPublishPackageVersion(objects) {

    /**
     * The asynchronous CodeArtifact client.
     * Set directly for BYO-client usage; [AsyncBatchPublishPackageVersion] wires this from a build service.
     */
    @get:Internal
    abstract val client: Property<CodeartifactAsyncClient>

    @Suppress("detekt:SpreadOperator")
    @TaskAction
    fun run() {
        val futures = requests.getOrElse(emptyList()).map { req ->
            client.get()
                .publishPackageVersion(req.request, AsyncRequestBody.fromFile(req.assetContent.asPath.get()))
                .thenApply { Unit }
        }
        CompletableFuture.allOf(*futures.toTypedArray<CompletableFuture<Unit>>()).join()
    }
}

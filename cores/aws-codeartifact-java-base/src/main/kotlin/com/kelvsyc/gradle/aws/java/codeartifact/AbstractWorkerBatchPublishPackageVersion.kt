package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * Abstract [AbstractBatchPublishPackageVersion] that uses the Gradle Worker API with a synchronous
 * [CodeArtifactClientBuildService] to publish multiple assets concurrently.
 *
 * Each artifact is submitted as a separate [PublishPackageVersionAction] worker action. Gradle's
 * worker executor manages thread-pool concurrency across submissions. There is no built-in retry support —
 * transient failures fail the worker action immediately.
 *
 * **BYO-service use:** Extend this class and set [service] directly. For automatic service registration via
 * `@ServiceReference`, extend [BatchPublishPackageVersion] instead.
 *
 * @see BatchPublishPackageVersion
 * @see AbstractAsyncBatchPublishPackageVersion
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class AbstractWorkerBatchPublishPackageVersion @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor,
) : AbstractBatchPublishPackageVersion(objects) {

    /**
     * The build service managing the synchronous CodeArtifact client.
     * Set directly for BYO-service usage; [BatchPublishPackageVersion] wires this via `@ServiceReference`.
     */
    @get:Internal
    abstract val service: Property<CodeArtifactClientBuildService>

    @TaskAction
    fun run() {
        val queue = workerExecutor.noIsolation()
        requests.getOrElse(emptyList()).forEach { req ->
            queue.submit(PublishPackageVersionAction::class.java) {
                it.service.set(service)
                it.domain.set(req.request.domain())
                it.domainOwner.set(req.request.domainOwner())
                it.repository.set(req.request.repository())
                it.namespace.set(req.request.namespace())
                it.packageValue.set(req.request.packageValue())
                it.packageVersion.set(req.request.packageVersion())
                it.assetName.set(req.request.assetName())
                it.assetSHA256.set(req.request.assetSHA256())
                it.assetContent.set(req.assetContent)
                if (req.request.unfinished() != null) {
                    it.unfinished.set(req.request.unfinished())
                }
            }
        }
    }
}

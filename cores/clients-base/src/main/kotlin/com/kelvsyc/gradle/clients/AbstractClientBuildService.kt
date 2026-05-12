package com.kelvsyc.gradle.clients

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Abstract base class for build services that manage a single service client instance.
 *
 * Each subclass defines its client configuration as serializable [BuildServiceParameters] properties, ensuring full
 * compatibility with Gradle's configuration cache. The client is created lazily on first access via [createClient]
 * and cached for the lifetime of the build service.
 *
 * Each client registration is its own [BuildService] instance, so Gradle can serialize the service reference and its
 * parameters natively. Consuming [org.gradle.api.provider.ValueSource] and [org.gradle.workers.WorkAction] parameters
 * hold a `Property<BuildService>`, which Gradle handles as part of its configuration cache support.
 *
 * ### Example
 *
 * ```kotlin
 * abstract class MyClientBuildService : AbstractClientBuildService<MyClient, MyClientBuildService.Params>() {
 *     interface Params : BuildServiceParameters {
 *         val endpoint: Property<String>
 *     }
 *
 *     override fun createClient(): MyClient = MyClient(parameters.endpoint.get())
 * }
 * ```
 *
 * @param C The type of the service client managed by this build service
 * @param P The [BuildServiceParameters] type containing the serializable configuration for client creation
 */
abstract class AbstractClientBuildService<C : Any, P : BuildServiceParameters> : BuildService<P>, AutoCloseable {
    private val lazyClient = lazy { createClient() }

    /**
     * Returns the managed client instance, creating it on first access.
     *
     * The returned instance is cached for the lifetime of this build service. Subsequent calls return the same
     * instance.
     */
    fun getClient(): C = lazyClient.value

    /**
     * Creates the service client from the build service parameters.
     *
     * Called exactly once during the lifetime of this build service instance, when [getClient] is first invoked.
     * Implementations should use [getParameters] to access the serializable configuration.
     */
    protected abstract fun createClient(): C

    override fun close() {
        if (lazyClient.isInitialized()) {
            (lazyClient.value as? AutoCloseable)?.close()
        }
    }
}

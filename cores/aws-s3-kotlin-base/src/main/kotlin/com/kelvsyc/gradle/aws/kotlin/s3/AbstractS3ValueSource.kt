package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * Base class for [ValueSource] implementations that provide a value by reading an S3 object.
 *
 * Subclasses should implement the [doObtain] function, transforming a [GetObjectResponse] to an object of the
 * desired type. This class should only be used on S3 objects for which the entire object can be kept in memory.
 *
 * **Configuration cache and sensitive objects:** Gradle serializes the result of every [ValueSource.obtain] call
 * to the configuration cache in plaintext when the cache is written. Whatever [doObtain] returns — including any
 * sensitive content the S3 object may contain (credentials, private keys, tokens) — will be stored in
 * `.gradle/configuration-cache/` and is readable by any process with access to the build directory. This applies
 * regardless of how the resulting [org.gradle.api.provider.Provider] is stored: wiring it into a task `@Input`
 * property, a `@get:Internal` property, or a private `val` all cause `obtain()` to run at configuration time and
 * the result to be cached.
 *
 * If the fetched object may contain sensitive data, call the [S3ClientBuildService] client directly inside a
 * [org.gradle.workers.WorkAction.execute] body instead, where the result is never written to the cache.
 * Non-sensitive objects (version manifests, metadata, changelogs) are safe to use at configuration time.
 */
abstract class AbstractS3ValueSource<T : Any, P : AbstractS3ValueSource.Parameters> : ValueSource<T, P> {
    interface Parameters : ValueSourceParameters {
        @get:Internal
        val service: Property<S3ClientBuildService>

        val bucket: Property<String>
        val key: Property<String>
    }

    abstract fun doObtain(response: GetObjectResponse): T?

    override fun obtain(): T? {
        val request = GetObjectRequest {
            bucket = parameters.bucket.get()
            key = parameters.key.get()
        }

        return runBlocking {
            parameters.service.get().getClient().getObject(request, ::doObtain)
        }
    }
}

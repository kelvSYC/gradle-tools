package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.api.gax.rpc.ApiException
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. A secret value returned
 * here will be stored in `.gradle/configuration-cache/` and is readable by any process with
 * access to the build directory. Retrieve secrets inside a
 * [org.gradle.workers.WorkAction] at task execution time instead, where the value is never
 * written to the cache.
 *
 * **Task-field storage is also unsafe.** Gradle's config-cache codec walks the entire task
 * object graph — including `@get:Internal` properties and private `val` fields — and resolves
 * all `Provider` values at cache-write time. Wiring a `Provider` backed by this `ValueSource`
 * into any task field (annotated or not) causes `obtain()` to run at configuration time and the
 * secret to be stored on disk. The only safe location is entirely within a `@TaskAction` or
 * `WorkAction.execute()` body. Using this `ValueSource` only inside task execution code is safe
 * but counterproductive — the abstraction adds no value there; call the build service client
 * directly instead.
 *
 * [ValueSource] implementation backed by retrieving a secret version from Google Cloud Secret Manager.
 *
 * The secret payload is returned as a UTF-8 string.
 */
@Deprecated(
    message = "ValueSource results are serialized to the Gradle configuration cache; the secret value " +
        "returned by obtain() is stored in plaintext in .gradle/configuration-cache/. " +
        "Retrieve secrets inside a WorkAction at task execution time instead, " +
        "where the value is never written to the cache.",
    level = DeprecationLevel.WARNING
)
abstract class SecretManagerValueSource : ValueSource<String, SecretManagerValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    interface Parameters : ValueSourceParameters {
        /** The build service managing the Secret Manager client. */
        @get:Internal
        val service: Property<SecretManagerServiceClientBuildService>

        val projectId: Property<String>
        val secretId: Property<String>

        /**
         * The version of the secret to retrieve. Defaults to `"latest"` if not set.
         */
        val versionId: Property<String>
    }

    override fun obtain(): String? {
        val version = if (parameters.versionId.isPresent) parameters.versionId.get() else "latest"
        val name = SecretVersionName.of(parameters.projectId.get(), parameters.secretId.get(), version)
        val request = AccessSecretVersionRequest.newBuilder().setName(name.toString()).build()

        return try {
            val response = parameters.service.get().getClient().accessSecretVersion(request)
            response.payload.data.toStringUtf8()
        } catch (e: ApiException) {
            logger.warn(e) { "Unable to retrieve secret '${parameters.secretId.get()}' from Google Cloud Secret Manager" }
            null
        }
    }
}

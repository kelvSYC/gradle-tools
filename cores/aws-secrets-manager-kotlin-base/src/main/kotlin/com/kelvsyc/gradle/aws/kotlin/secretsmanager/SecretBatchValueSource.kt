package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.model.BatchGetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.paginators.batchGetSecretValuePaginated
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. Secret values returned
 * here will be stored in `.gradle/configuration-cache/` and are readable by any process with
 * access to the build directory. Retrieve secrets inside a
 * [org.gradle.workers.WorkAction] at task execution time instead, where the value is never
 * written to the cache.
 *
 * **Task-field storage is also unsafe.** Gradle's config-cache codec walks the entire task
 * object graph — including `@get:Internal` properties and private `val` fields — and resolves
 * all `Provider` values at cache-write time. Wiring a `Provider` backed by this `ValueSource`
 * into any task field (annotated or not) causes `obtain()` to run at configuration time and the
 * secrets to be stored on disk. The only safe location is entirely within a `@TaskAction` or
 * `WorkAction.execute()` body. Using this `ValueSource` only inside task execution code is safe
 * but counterproductive — the abstraction adds no value there; call the build service client
 * directly instead.
 *
 * [ValueSource] implementation that retrieves a set of secrets, by their IDs, from Secrets Manager.
 *
 * Only string secrets are supported.
 */
@Deprecated(
    message = "ValueSource results are serialized to the Gradle configuration cache; secret values " +
        "returned by obtain() are stored in plaintext in .gradle/configuration-cache/. " +
        "Retrieve secrets inside a WorkAction at task execution time instead, " +
        "where the value is never written to the cache.",
    level = DeprecationLevel.WARNING
)
abstract class SecretBatchValueSource : ValueSource<Map<String, String>, SecretBatchValueSource.Parameters> {
    /**
     * Parameters for [SecretBatchValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Secrets Manager client. */
        @get:Internal
        val service: Property<SecretsManagerClientBuildService>

        /** Set of secret IDs (names or ARNs) to retrieve. */
        val secretIds: SetProperty<String>
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = BatchGetSecretValueRequest {
            secretIdList = parameters.secretIds.get().toList()
        }
        val client = parameters.service.get().getClient()

        return runBlocking {
            client.batchGetSecretValuePaginated(request)
                .flatMapLatest { it.secretValues?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.name!!] = value.secretString!!
                    }
                }
        }
    }
}

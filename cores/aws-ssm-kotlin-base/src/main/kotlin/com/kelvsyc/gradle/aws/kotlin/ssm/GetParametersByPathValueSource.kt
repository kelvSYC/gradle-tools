package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.model.GetParametersByPathRequest
import aws.sdk.kotlin.services.ssm.paginators.getParametersByPathPaginated
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves all SSM parameters under a given hierarchy [Parameters.path],
 * returned as a [Map] keyed by parameter name.
 *
 * Pagination is handled internally via the SDK Kotlin paginated flow. For `SecureString` parameters, set
 * [Parameters.withDecryption] to `true` to decrypt the values.
 */
abstract class GetParametersByPathValueSource :
    ValueSource<Map<String, String>, GetParametersByPathValueSource.Parameters> {
    /**
     * Parameters for [GetParametersByPathValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the SSM client. */
        @get:Internal
        val service: Property<SsmClientBuildService>

        /** The hierarchy path under which parameters are retrieved (e.g. `/my/app/`). */
        val path: Property<String>

        /** Whether to retrieve all parameters in the hierarchy recursively. Defaults to `false`. */
        val recursive: Property<Boolean>

        /** Whether to decrypt `SecureString` parameter values. Defaults to `false`. */
        val withDecryption: Property<Boolean>
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun obtain(): Map<String, String>? {
        val request = GetParametersByPathRequest {
            path = parameters.path.get()
            recursive = parameters.recursive.orNull
            withDecryption = parameters.withDecryption.orNull
        }
        val client = parameters.service.get().getClient()

        return runBlocking {
            client.getParametersByPathPaginated(request)
                .flatMapLatest { it.parameters?.asFlow() ?: emptyFlow() }
                .fold(mutableMapOf()) { acc, value ->
                    acc.also {
                        it[value.name!!] = value.value!!
                    }
                }
        }
    }
}

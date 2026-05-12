package com.kelvsyc.gradle.aws.java.ssm

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest
import kotlin.streams.asSequence
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves all SSM parameters under a given hierarchy [Parameters.path],
 * returned as a [Map] keyed by parameter name.
 *
 * Pagination is handled internally via the SDK Java paginator. For `SecureString` parameters, set
 * [Parameters.withDecryption] to `true` to decrypt the values.
 */
abstract class GetParametersByPathValueSource : ValueSource<Map<String, String>, GetParametersByPathValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing SSM clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [SsmClientInfo]. */
        val clientName: Property<String>

        /** The hierarchy path under which parameters are retrieved (e.g. `/my/app/`). */
        val path: Property<String>

        /** Whether to retrieve all parameters in the hierarchy recursively. Defaults to `false`. */
        val recursive: Property<Boolean>

        /** Whether to decrypt `SecureString` parameter values. Defaults to `false`. */
        val withDecryption: Property<Boolean>
    }

    private val client: Provider<SsmClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Map<String, String>? {
        val request = GetParametersByPathRequest.builder().apply {
            path(parameters.path.get())
            if (parameters.recursive.isPresent) {
                recursive(parameters.recursive.get())
            }
            if (parameters.withDecryption.isPresent) {
                withDecryption(parameters.withDecryption.get())
            }
        }.build()

        val response = client.get()
            .getParametersByPathPaginator(request)
            .stream()
            .asSequence()

        return response
            .flatMap { it.parameters() }
            .associate {
                it.name() to it.value()
            }
    }
}

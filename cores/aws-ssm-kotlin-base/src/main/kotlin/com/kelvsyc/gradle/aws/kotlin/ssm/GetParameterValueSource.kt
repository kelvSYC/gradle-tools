package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.GetParameterRequest
import aws.sdk.kotlin.services.ssm.model.SsmException
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation backed by retrieving a single parameter from SSM Parameter Store.
 *
 * Returns the parameter value as a string. For `SecureString` parameters, set [Parameters.withDecryption] to
 * `true` to decrypt the value.
 */
abstract class GetParameterValueSource : ValueSource<String, GetParameterValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing SSM clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [SsmClientInfo]. */
        val clientName: Property<String>

        /** The name (or ARN) of the parameter to retrieve. */
        val parameterName: Property<String>

        /** Whether to decrypt `SecureString` parameter values. Defaults to `false`. */
        val withDecryption: Property<Boolean>
    }

    private val client: Provider<SsmClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = GetParameterRequest {
            name = parameters.parameterName.get()
            withDecryption = parameters.withDecryption.orNull
        }

        return try {
            runBlocking {
                client.get().getParameter(request).parameter?.value
            }
        } catch (e: SsmException) {
            logger.warn("Unable to retrieve parameter '${parameters.parameterName.get()}' from AWS", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetParameterValueSource::class.java)
    }
}

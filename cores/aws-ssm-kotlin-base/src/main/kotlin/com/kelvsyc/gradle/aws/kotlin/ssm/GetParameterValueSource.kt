package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.model.GetParameterRequest
import aws.sdk.kotlin.services.ssm.model.SsmException
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation backed by retrieving a single parameter from SSM Parameter Store.
 *
 * Returns the parameter value as a string. For `SecureString` parameters, set [Parameters.withDecryption] to
 * `true` to decrypt the value.
 */
abstract class GetParameterValueSource : ValueSource<String, GetParameterValueSource.Parameters> {
    /**
     * Parameters for [GetParameterValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the SSM client. */
        val service: Property<SsmClientBuildService>

        /** The name (or ARN) of the parameter to retrieve. */
        val parameterName: Property<String>

        /** Whether to decrypt `SecureString` parameter values. Defaults to `false`. */
        val withDecryption: Property<Boolean>
    }

    override fun obtain(): String? {
        val request = GetParameterRequest {
            name = parameters.parameterName.get()
            withDecryption = parameters.withDecryption.orNull
        }

        return try {
            runBlocking {
                parameters.service.get().getClient().getParameter(request).parameter?.value
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

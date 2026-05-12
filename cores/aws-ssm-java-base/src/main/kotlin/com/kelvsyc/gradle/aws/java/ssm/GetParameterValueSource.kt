package com.kelvsyc.gradle.aws.java.ssm

import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import software.amazon.awssdk.services.ssm.model.SsmException

/**
 * [ValueSource] implementation backed by retrieving a single parameter from SSM Parameter Store.
 *
 * Returns the parameter value as a string. For `SecureString` parameters, set [Parameters.withDecryption] to
 * `true` to decrypt the value.
 */
abstract class GetParameterValueSource : ValueSource<String, GetParameterValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

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
        val request = GetParameterRequest.builder().apply {
            name(parameters.parameterName.get())
            if (parameters.withDecryption.isPresent) {
                withDecryption(parameters.withDecryption.get())
            }
        }.build()

        return try {
            parameters.service.get().getClient().getParameter(request).parameter()?.value()
        } catch (e: SsmException) {
            logger.warn(e) { "Unable to retrieve parameter '${parameters.parameterName.get()}' from AWS" }
            null
        }
    }
}

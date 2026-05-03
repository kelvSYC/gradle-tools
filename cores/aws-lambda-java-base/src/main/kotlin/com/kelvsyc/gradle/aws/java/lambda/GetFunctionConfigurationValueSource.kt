package com.kelvsyc.gradle.aws.java.lambda

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationRequest
import software.amazon.awssdk.services.lambda.model.LambdaException

/**
 * [ValueSource] implementation that retrieves the ARN of a Lambda function's published configuration.
 *
 * Returns the function ARN (qualified by version or alias when [Parameters.qualifier] is set). Returns `null`
 * and logs a warning when the call throws [LambdaException] (e.g. function not found).
 */
abstract class GetFunctionConfigurationValueSource : ValueSource<String, GetFunctionConfigurationValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    interface Parameters : ValueSourceParameters {
        /** The shared build service managing Lambda clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [LambdaClientInfo]. */
        val clientName: Property<String>

        /** The function name, ARN, or partial ARN. */
        val functionName: Property<String>

        /** Optional version or alias qualifier. */
        val qualifier: Property<String>
    }

    private val client: Provider<LambdaClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = GetFunctionConfigurationRequest.builder().apply {
            functionName(parameters.functionName.get())
            if (parameters.qualifier.isPresent) {
                qualifier(parameters.qualifier.get())
            }
        }.build()

        return try {
            val response = client.get().getFunctionConfiguration(request)
            response.functionArn()
        } catch (e: LambdaException) {
            logger.warn(e) { "Unable to retrieve configuration for Lambda function '${parameters.functionName.get()}'" }
            null
        }
    }
}

package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.model.GetFunctionConfigurationRequest
import aws.sdk.kotlin.services.lambda.model.LambdaException
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves the ARN of a Lambda function's published configuration.
 *
 * Returns the function ARN (qualified by version or alias when [Parameters.qualifier] is set). Returns `null`
 * and logs a warning when the call throws [LambdaException] (e.g. function not found).
 */
abstract class GetFunctionConfigurationValueSource :
    ValueSource<String, GetFunctionConfigurationValueSource.Parameters> {
    /**
     * Parameters for [GetFunctionConfigurationValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Lambda client. */
        @get:Internal
        val service: Property<LambdaClientBuildService>

        /** The function name, ARN, or partial ARN. */
        val functionName: Property<String>

        /** Optional version or alias qualifier. */
        val qualifier: Property<String>
    }

    override fun obtain(): String? {
        val request = GetFunctionConfigurationRequest {
            functionName = parameters.functionName.get()
            qualifier = parameters.qualifier.orNull
        }

        return try {
            runBlocking {
                parameters.service.get().getClient().getFunctionConfiguration(request).functionArn
            }
        } catch (e: LambdaException) {
            logger.warn("Unable to retrieve configuration for Lambda function '${parameters.functionName.get()}'", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(GetFunctionConfigurationValueSource::class.java)
    }
}

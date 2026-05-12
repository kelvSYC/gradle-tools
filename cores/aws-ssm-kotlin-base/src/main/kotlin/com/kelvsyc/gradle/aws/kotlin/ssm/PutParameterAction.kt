package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.model.ParameterType
import aws.sdk.kotlin.services.ssm.model.PutParameterRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that creates or updates a parameter in SSM Parameter Store.
 *
 * To update an existing parameter, set [Parameters.overwrite] to `true`. The [Parameters.parameterType] is only
 * required when creating a new parameter; for updates of an existing parameter the type may be omitted.
 */
abstract class PutParameterAction : WorkAction<PutParameterAction.Parameters> {
    /**
     * Parameters for [PutParameterAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the SSM client. */
        val service: Property<SsmClientBuildService>

        /** The name of the parameter to create or update. */
        val parameterName: Property<String>

        /** The new value for the parameter. */
        val parameterValue: Property<String>

        /** The parameter type (one of `String`, `StringList`, `SecureString`); required when creating a parameter. */
        val parameterType: Property<String>

        /** Whether to overwrite an existing parameter with the same name. Defaults to `false`. */
        val overwrite: Property<Boolean>
    }

    override fun execute() {
        val request = PutParameterRequest {
            name = parameters.parameterName.get()
            value = parameters.parameterValue.get()
            type = parameters.parameterType.orNull?.let { ParameterType.fromValue(it) }
            overwrite = parameters.overwrite.orNull
        }

        runBlocking {
            parameters.service.get().getClient().putParameter(request)
        }
    }
}

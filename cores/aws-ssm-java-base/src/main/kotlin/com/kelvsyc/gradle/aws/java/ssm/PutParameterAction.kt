package com.kelvsyc.gradle.aws.java.ssm

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.ParameterType
import software.amazon.awssdk.services.ssm.model.PutParameterRequest

/**
 * [WorkAction] implementation that creates or updates a parameter in SSM Parameter Store.
 *
 * To update an existing parameter, set [Parameters.overwrite] to `true`. The [Parameters.parameterType] is only
 * required when creating a new parameter; for updates of an existing parameter the type may be omitted.
 */
abstract class PutParameterAction : WorkAction<PutParameterAction.Parameters> {
    interface Parameters : WorkParameters {
        /** The shared build service managing SSM clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of an [SsmClientInfo]. */
        val clientName: Property<String>

        /** The name of the parameter to create or update. */
        val parameterName: Property<String>

        /** The new value for the parameter. */
        val parameterValue: Property<String>

        /** The parameter type (one of `String`, `StringList`, `SecureString`); required when creating a parameter. */
        val parameterType: Property<String>

        /** Whether to overwrite an existing parameter with the same name. Defaults to `false`. */
        val overwrite: Property<Boolean>
    }

    private val client: Provider<SsmClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = PutParameterRequest.builder().apply {
            name(parameters.parameterName.get())
            value(parameters.parameterValue.get())
            if (parameters.parameterType.isPresent) {
                type(ParameterType.fromValue(parameters.parameterType.get()))
            }
            if (parameters.overwrite.isPresent) {
                overwrite(parameters.overwrite.get())
            }
        }.build()

        client.get().putParameter(request)
    }
}
